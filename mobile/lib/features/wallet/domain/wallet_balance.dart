/// Solde du portefeuille de l'utilisateur connecté.
class WalletBalance {
  const WalletBalance({required this.balance, required this.currency});

  /// Solde, en FCFA (entier).
  final int balance;
  final String currency;

  factory WalletBalance.fromJson(Map<String, dynamic> json) {
    return WalletBalance(
      balance: (json['balance'] as num).toInt(),
      currency: json['currency'] as String,
    );
  }
}
