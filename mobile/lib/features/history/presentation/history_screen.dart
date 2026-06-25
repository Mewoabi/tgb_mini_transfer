import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/presentation/widgets/async_state_widgets.dart';
import '../../../core/presentation/widgets/transaction_tile.dart';
import '../application/history_controller.dart';
import '../domain/transaction_item.dart';

/// Filtre d'affichage de l'historique.
enum HistoryFilter { all, sent, received }

/// Écran d'historique des transactions (corps d'onglet) avec filtres.
class HistoryScreen extends ConsumerStatefulWidget {
  const HistoryScreen({super.key});

  @override
  ConsumerState<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends ConsumerState<HistoryScreen> {
  HistoryFilter _filter = HistoryFilter.all;

  List<TransactionItem> _applyFilter(List<TransactionItem> items) {
    return switch (_filter) {
      HistoryFilter.all => items,
      HistoryFilter.sent =>
        items.where((item) => item.direction == TransactionDirection.sent).toList(),
      HistoryFilter.received =>
        items.where((item) => item.direction == TransactionDirection.received).toList(),
    };
  }

  @override
  Widget build(BuildContext context) {
    final historyAsync = ref.watch(historyControllerProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
          child: SegmentedButton<HistoryFilter>(
            segments: const [
              ButtonSegment(value: HistoryFilter.all, label: Text('Tous')),
              ButtonSegment(value: HistoryFilter.sent, label: Text('Émis')),
              ButtonSegment(value: HistoryFilter.received, label: Text('Reçus')),
            ],
            selected: {_filter},
            onSelectionChanged: (selection) => setState(() => _filter = selection.first),
          ),
        ),
        Expanded(
          child: RefreshIndicator(
            onRefresh: () => ref.read(historyControllerProvider.notifier).refresh(),
            child: historyAsync.when(
              loading: () => const ScrollableCenter(child: CircularProgressIndicator()),
              error: (error, _) => ScrollableCenter(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: ErrorState(
                    message: error is ApiException ? error.message : 'Une erreur est survenue.',
                    onRetry: () => ref.read(historyControllerProvider.notifier).refresh(),
                  ),
                ),
              ),
              data: (items) {
                final filtered = _applyFilter(items);
                if (filtered.isEmpty) {
                  return ScrollableCenter(
                    child: EmptyState(
                      message: _filter == HistoryFilter.all
                          ? 'Aucune transaction pour le moment.'
                          : 'Aucune transaction dans cette catégorie.',
                      icon: Icons.receipt_long_outlined,
                    ),
                  );
                }
                return ListView.separated(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  itemCount: filtered.length,
                  separatorBuilder: (_, _) => const Divider(height: 1, indent: 72),
                  itemBuilder: (_, index) => TransactionTile(item: filtered[index]),
                );
              },
            ),
          ),
        ),
      ],
    );
  }
}
