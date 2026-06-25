import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/utils/money.dart';
import '../application/history_controller.dart';
import '../domain/transaction_item.dart';

/// Écran d'historique des transactions (émises et reçues), de la plus récente à la plus ancienne.
class HistoryScreen extends ConsumerWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final historyAsync = ref.watch(historyControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Historique')),
      body: RefreshIndicator(
        onRefresh: () => ref.read(historyControllerProvider.notifier).refresh(),
        child: historyAsync.when(
          loading: () => const _ScrollableCenter(child: CircularProgressIndicator()),
          error: (error, _) => _ScrollableCenter(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const Icon(Icons.error_outline, size: 48),
                  const SizedBox(height: 16),
                  Text(
                    error is ApiException ? error.message : 'Une erreur est survenue.',
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  OutlinedButton(
                    onPressed: () => ref.read(historyControllerProvider.notifier).refresh(),
                    child: const Text('Réessayer'),
                  ),
                ],
              ),
            ),
          ),
          data: (items) {
            if (items.isEmpty) {
              return const _ScrollableCenter(child: Text('Aucune transaction pour le moment.'));
            }
            return ListView.separated(
              itemCount: items.length,
              separatorBuilder: (_, _) => const Divider(height: 1),
              itemBuilder: (_, index) => _TransactionTile(item: items[index]),
            );
          },
        ),
      ),
    );
  }
}

/// Ligne d'une transaction.
class _TransactionTile extends StatelessWidget {
  const _TransactionTile({required this.item});

  final TransactionItem item;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isSent = item.direction == TransactionDirection.sent;
    final color = isSent ? theme.colorScheme.error : Colors.green.shade700;
    final sign = isSent ? '-' : '+';
    final dateLabel = DateFormat('dd/MM/yyyy HH:mm').format(item.timestamp.toLocal());

    return ListTile(
      leading: CircleAvatar(
        backgroundColor: color.withValues(alpha: 0.15),
        child: Icon(isSent ? Icons.arrow_upward : Icons.arrow_downward, color: color),
      ),
      title: Text(item.counterpartyName),
      subtitle: Text('$dateLabel · ${item.status}'),
      trailing: Text(
        '$sign ${formatFcfa(item.amount)}',
        style: theme.textTheme.titleMedium?.copyWith(color: color, fontWeight: FontWeight.bold),
      ),
    );
  }
}

/// Contenu centré mais défilable (pour conserver le « pull-to-refresh » dans tous les états).
class _ScrollableCenter extends StatelessWidget {
  const _ScrollableCenter({required this.child});

  final Widget child;

  @override
  Widget build(BuildContext context) {
    return ListView(
      physics: const AlwaysScrollableScrollPhysics(),
      children: [
        SizedBox(
          height: MediaQuery.sizeOf(context).height * 0.6,
          child: Center(child: child),
        ),
      ],
    );
  }
}
