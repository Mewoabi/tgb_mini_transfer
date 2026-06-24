package com.tgbsolutions.minitransfer.transfer;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Transaction enregistrée à l'issue d'un transfert (collection « transactions »).
 *
 * <p>Les noms de l'émetteur et du destinataire sont dénormalisés (copiés) dans le document
 * afin d'afficher l'historique sans relire les utilisateurs. Le montant est un entier (FCFA).</p>
 */
@Document(collection = "transactions")
public class Transaction {

	@Id
	private String id;

	/** Identifiant de l'émetteur (indexé pour la recherche d'historique). */
	@Indexed
	private String senderId;

	/** Identifiant du destinataire (indexé pour la recherche d'historique). */
	@Indexed
	private String recipientId;

	/** Nom de l'émetteur au moment du transfert (instantané pour l'affichage). */
	private String senderName;

	/** Nom du destinataire au moment du transfert (instantané pour l'affichage). */
	private String recipientName;

	/** Montant transféré, en FCFA (entier, strictement positif). */
	private long amount;

	/** Date et heure du transfert. */
	private Instant timestamp;

	/** Statut de la transaction. */
	private TransactionStatus status;

	protected Transaction() {
		// Constructeur requis par Spring Data.
	}

	public Transaction(String senderId, String recipientId, String senderName, String recipientName,
			long amount, Instant timestamp, TransactionStatus status) {
		this.senderId = senderId;
		this.recipientId = recipientId;
		this.senderName = senderName;
		this.recipientName = recipientName;
		this.amount = amount;
		this.timestamp = timestamp;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getRecipientId() {
		return recipientId;
	}

	public void setRecipientId(String recipientId) {
		this.recipientId = recipientId;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		this.amount = amount;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public TransactionStatus getStatus() {
		return status;
	}

	public void setStatus(TransactionStatus status) {
		this.status = status;
	}
}
