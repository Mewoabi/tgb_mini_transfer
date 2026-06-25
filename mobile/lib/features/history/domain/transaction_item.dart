/// Sens d'une transaction du point de vue de l'utilisateur courant.
enum TransactionDirection { sent, received }

/// Élément d'historique des transactions.
class TransactionItem {
  const TransactionItem({
    required this.id,
    required this.direction,
    required this.counterpartyName,
    required this.amount,
    required this.timestamp,
    required this.status,
  });

  final String id;
  final TransactionDirection direction;

  /// Nom de l'autre partie (destinataire si émise, émetteur si reçue).
  final String counterpartyName;

  /// Montant, en FCFA.
  final int amount;
  final DateTime timestamp;
  final String status;

  factory TransactionItem.fromJson(Map<String, dynamic> json) {
    return TransactionItem(
      id: json['transactionId'] as String,
      direction: (json['direction'] as String) == 'SENT'
          ? TransactionDirection.sent
          : TransactionDirection.received,
      counterpartyName: json['counterpartyName'] as String,
      amount: (json['amount'] as num).toInt(),
      timestamp: DateTime.parse(json['timestamp'] as String),
      status: json['status'] as String,
    );
  }
}
