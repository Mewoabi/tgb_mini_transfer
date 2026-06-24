package com.tgbsolutions.minitransfer.transfer;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Dépôt MongoDB des transactions.
 */
public interface TransactionRepository extends MongoRepository<Transaction, String> {

	/**
	 * Historique d'un utilisateur : transactions émises ou reçues, triées par date décroissante.
	 * Pour un utilisateur donné, on passe son identifiant à la fois comme émetteur et comme destinataire.
	 */
	List<Transaction> findBySenderIdOrRecipientIdOrderByTimestampDesc(String senderId, String recipientId);
}
