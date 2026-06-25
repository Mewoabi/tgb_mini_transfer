package com.tgbsolutions.minitransfer.transfer;

import com.mongodb.client.result.UpdateResult;
import com.tgbsolutions.minitransfer.common.InsufficientBalanceException;
import com.tgbsolutions.minitransfer.common.InvalidTransferException;
import com.tgbsolutions.minitransfer.common.ResourceNotFoundException;
import com.tgbsolutions.minitransfer.transfer.dto.TransactionDirection;
import com.tgbsolutions.minitransfer.transfer.dto.TransactionHistoryItem;
import com.tgbsolutions.minitransfer.transfer.dto.TransferRequest;
import com.tgbsolutions.minitransfer.transfer.dto.TransferResponse;
import com.tgbsolutions.minitransfer.user.User;
import com.tgbsolutions.minitransfer.user.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service de transfert d'argent.
 *
 * <p>L'opération est transactionnelle ({@code @Transactional}) : le débit de l'émetteur, le crédit
 * du destinataire et l'enregistrement de la transaction réussissent ou échouent ensemble. On ne
 * perd ni ne crée jamais d'argent.</p>
 */
@Service
public class TransferService {

	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;
	private final MongoTemplate mongoTemplate;

	public TransferService(UserRepository userRepository, TransactionRepository transactionRepository,
			MongoTemplate mongoTemplate) {
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
		this.mongoTemplate = mongoTemplate;
	}

	/**
	 * Effectue un transfert depuis l'émetteur (identifié par son id, issu du token) vers le
	 * destinataire (identifié par email ou téléphone).
	 */
	@Transactional
	public TransferResponse transfer(String senderId, TransferRequest request) {
		long amount = request.amount();
		if (amount <= 0) {
			throw new InvalidTransferException("Le montant doit être strictement positif.");
		}

		User sender = userRepository.findById(senderId)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));

		User recipient = resolveRecipient(request.recipient());

		if (recipient.getId().equals(sender.getId())) {
			throw new InvalidTransferException("Un transfert vers soi-même n'est pas autorisé.");
		}

		// Débit atomique et conditionnel : ne débite que si le solde est suffisant.
		// modifiedCount == 0 signifie « solde insuffisant » (l'émetteur, lui, existe).
		Query debitQuery = Query.query(
				Criteria.where("_id").is(sender.getId()).and("balance").gte(amount));
		UpdateResult debit = mongoTemplate.updateFirst(debitQuery, new Update().inc("balance", -amount), User.class);
		if (debit.getModifiedCount() == 0) {
			throw new InsufficientBalanceException("Solde insuffisant pour effectuer ce transfert.");
		}

		// Crédit du destinataire (incrément atomique).
		mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(recipient.getId())),
				new Update().inc("balance", amount), User.class);

		// Enregistrement de la transaction (noms figés pour l'affichage de l'historique).
		Transaction transaction = new Transaction(sender.getId(), recipient.getId(),
				sender.getName(), recipient.getName(), amount, Instant.now(), TransactionStatus.COMPLETED);
		transaction = transactionRepository.save(transaction);

		long newBalance = sender.getBalance() - amount;
		return new TransferResponse(transaction.getId(), recipient.getName(), amount,
				transaction.getTimestamp(), transaction.getStatus(), newBalance);
	}

	/**
	 * Historique des transactions de l'utilisateur (émises et reçues), triées par date décroissante.
	 * Chaque élément est présenté selon le sens (émise/reçue) du point de vue de l'utilisateur.
	 */
	public List<TransactionHistoryItem> getHistory(String userId) {
		return transactionRepository.findBySenderIdOrRecipientIdOrderByTimestampDesc(userId, userId)
				.stream()
				.map(transaction -> toHistoryItem(transaction, userId))
				.toList();
	}

	private TransactionHistoryItem toHistoryItem(Transaction transaction, String userId) {
		boolean sent = transaction.getSenderId().equals(userId);
		TransactionDirection direction = sent ? TransactionDirection.SENT : TransactionDirection.RECEIVED;
		String counterparty = sent ? transaction.getRecipientName() : transaction.getSenderName();
		return new TransactionHistoryItem(transaction.getId(), direction, counterparty,
				transaction.getAmount(), transaction.getTimestamp(), transaction.getStatus());
	}

	/** Résout le destinataire par email, puis par téléphone. */
	private User resolveRecipient(String identifier) {
		return userRepository.findByEmail(identifier)
				.or(() -> userRepository.findByPhone(identifier))
				.orElseThrow(() -> new ResourceNotFoundException("Destinataire introuvable."));
	}
}
