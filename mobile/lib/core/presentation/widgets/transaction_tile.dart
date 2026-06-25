import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../core/utils/money.dart';
import '../../../features/history/domain/transaction_item.dart';

/// Ligne d'une transaction (partagée entre l'accueil et l'historique).
class TransactionTile extends StatelessWidget {
  const TransactionTile({super.key, required this.item, this.compact = false});

  final TransactionItem item;
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isSent = item.direction == TransactionDirection.sent;
    final color = isSent ? theme.colorScheme.error : Colors.green.shade700;
    final sign = isSent ? '-' : '+';
    final dateLabel = DateFormat('dd/MM/yyyy HH:mm').format(item.timestamp.toLocal());

    return ListTile(
      contentPadding: compact ? const EdgeInsets.symmetric(horizontal: 8) : null,
      leading: CircleAvatar(
        backgroundColor: color.withValues(alpha: 0.15),
        child: Icon(
          isSent ? Icons.arrow_upward : Icons.arrow_downward,
          color: color,
          size: compact ? 18 : 24,
        ),
      ),
      title: Text(item.counterpartyName, maxLines: 1, overflow: TextOverflow.ellipsis),
      subtitle: Text(
        compact ? dateLabel : '$dateLabel · ${item.status}',
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      trailing: Text(
        '$sign ${formatFcfa(item.amount)}',
        style: theme.textTheme.titleMedium?.copyWith(
          color: color,
          fontWeight: FontWeight.bold,
          fontSize: compact ? 14 : null,
        ),
      ),
    );
  }
}
