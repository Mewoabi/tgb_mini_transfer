/// Résultat d'un transfert effectué (sous-ensemble utile à l'UI).
class TransferResult {
  const TransferResult({
    required this.recipientName,
    required this.amount,
    required this.newBalance,
  });

  final String recipientName;

  /// Montant transféré, en FCFA.
  final int amount;

  /// Nouveau solde de l'émetteur après le transfert, en FCFA.
  final int newBalance;

  factory TransferResult.fromJson(Map<String, dynamic> json) {
    return TransferResult(
      recipientName: json['recipientName'] as String,
      amount: (json['amount'] as num).toInt(),
      newBalance: (json['newBalance'] as num).toInt(),
    );
  }
}
