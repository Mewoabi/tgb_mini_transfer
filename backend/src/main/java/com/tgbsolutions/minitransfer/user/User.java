package com.tgbsolutions.minitransfer.user;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Utilisateur de MiniTransfer.
 *
 * <p>Le solde du portefeuille est embarqué directement dans ce document (modèle
 * « users + solde » suggéré par le sujet) : relation 1-1 entre l'utilisateur et son
 * portefeuille, moins de documents touchés lors d'un transfert et mises à jour atomiques
 * plus simples. Le solde est un entier en FCFA, jamais un flottant.</p>
 */
@Document(collection = "users")
public class User {

	@Id
	private String id;

	private String name;

	/** Email unique : identifiant de connexion et cible possible d'un transfert. */
	@Indexed(unique = true)
	private String email;

	/** Numéro de téléphone unique : autre cible possible d'un transfert. */
	@Indexed(unique = true)
	private String phone;

	/** Empreinte BCrypt du mot de passe (jamais le mot de passe en clair). */
	private String passwordHash;

	/** Solde du portefeuille, en FCFA (entier). */
	private long balance;

	/** Date de création du compte. */
	private Instant createdAt;

	protected User() {
		// Constructeur requis par Spring Data.
	}

	public User(String name, String email, String phone, String passwordHash, long balance, Instant createdAt) {
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.passwordHash = passwordHash;
		this.balance = balance;
		this.createdAt = createdAt;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public long getBalance() {
		return balance;
	}

	public void setBalance(long balance) {
		this.balance = balance;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
