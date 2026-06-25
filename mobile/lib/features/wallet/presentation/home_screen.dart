import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/presentation/widgets/async_state_widgets.dart';
import '../../../core/presentation/widgets/transaction_tile.dart';
import '../../../core/utils/greeting.dart';
import '../../../core/utils/money.dart';
import '../../auth/application/current_user_controller.dart';
import '../../history/application/history_controller.dart';
import '../../history/domain/transaction_item.dart';
import '../application/wallet_controller.dart';
import '../domain/wallet_balance.dart';

/// Tableau de bord : salutation, solde, statistiques et transactions récentes.
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final balanceAsync = ref.watch(walletControllerProvider);
    final historyAsync = ref.watch(historyControllerProvider);
    final user = ref.watch(currentUserProvider);

    return RefreshIndicator(
      onRefresh: () async {
        await Future.wait([
          ref.read(walletControllerProvider.notifier).refresh(),
          ref.read(historyControllerProvider.notifier).refresh(),
        ]);
      },
      child: ListView(
        padding: const EdgeInsets.all(20),
        physics: const AlwaysScrollableScrollPhysics(),
        children: [
          _GreetingHeader(userName: user?.name),
          const SizedBox(height: 20),
          balanceAsync.when(
            loading: () => const _BalanceCardSkeleton(),
            error: (error, _) => ErrorState(
              message: error is ApiException ? error.message : 'Une erreur est survenue.',
              onRetry: () => ref.read(walletControllerProvider.notifier).refresh(),
            ),
            data: (balance) => _BalanceCard(balance: balance),
          ),
          const SizedBox(height: 20),
          historyAsync.when(
            loading: () => const SizedBox.shrink(),
            error: (_, _) => const SizedBox.shrink(),
            data: (items) => _QuickStatsRow(items: items),
          ),
          const SizedBox(height: 24),
          _RecentTransactionsSection(
            historyAsync: historyAsync,
            onSeeAll: () => StatefulNavigationShell.of(context).goBranch(2),
            onRetryHistory: () => ref.read(historyControllerProvider.notifier).refresh(),
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}

/// Salutation personnalisée avec le prénom de l'utilisateur.
class _GreetingHeader extends StatelessWidget {
  const _GreetingHeader({this.userName});

  final String? userName;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final greeting = userName != null
        ? 'Bonjour, ${GreetingUtils.firstName(userName!)}'
        : 'Bonjour';

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          greeting,
          style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 4),
        Text(
          'Voici un aperçu de votre portefeuille',
          style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.outline),
        ),
      ],
    );
  }
}

/// Carte de solde avec dégradé.
class _BalanceCard extends StatelessWidget {
  const _BalanceCard({required this.balance});

  final WalletBalance balance;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      elevation: 2,
      clipBehavior: Clip.antiAlias,
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.symmetric(vertical: 28, horizontal: 24),
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              theme.colorScheme.primary,
              theme.colorScheme.primaryContainer,
            ],
          ),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Solde disponible',
              style: theme.textTheme.titleMedium?.copyWith(
                color: theme.colorScheme.onPrimary.withValues(alpha: 0.9),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              formatFcfa(balance.balance),
              style: theme.textTheme.headlineMedium?.copyWith(
                color: theme.colorScheme.onPrimary,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Placeholder pendant le chargement du solde.
class _BalanceCardSkeleton extends StatelessWidget {
  const _BalanceCardSkeleton();

  @override
  Widget build(BuildContext context) {
    return const Card(
      child: SizedBox(
        height: 120,
        child: Center(child: CircularProgressIndicator()),
      ),
    );
  }
}

/// Statistiques rapides : total envoyé et total reçu.
class _QuickStatsRow extends StatelessWidget {
  const _QuickStatsRow({required this.items});

  final List<TransactionItem> items;

  @override
  Widget build(BuildContext context) {
    final sentTotal = items
        .where((item) => item.direction == TransactionDirection.sent)
        .fold<int>(0, (sum, item) => sum + item.amount);
    final receivedTotal = items
        .where((item) => item.direction == TransactionDirection.received)
        .fold<int>(0, (sum, item) => sum + item.amount);

    return Row(
      children: [
        Expanded(child: _StatCard(
          label: 'Total envoyé',
          amount: sentTotal,
          icon: Icons.arrow_upward,
          color: Theme.of(context).colorScheme.error,
        )),
        const SizedBox(width: 12),
        Expanded(child: _StatCard(
          label: 'Total reçu',
          amount: receivedTotal,
          icon: Icons.arrow_downward,
          color: Colors.green.shade700,
        )),
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({
    required this.label,
    required this.amount,
    required this.icon,
    required this.color,
  });

  final String label;
  final int amount;
  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icon, color: color, size: 20),
            const SizedBox(height: 8),
            Text(label, style: theme.textTheme.labelMedium),
            const SizedBox(height: 4),
            Text(
              formatFcfa(amount),
              style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }
}

/// Section des 5 transactions les plus récentes.
class _RecentTransactionsSection extends StatelessWidget {
  const _RecentTransactionsSection({
    required this.historyAsync,
    required this.onSeeAll,
    required this.onRetryHistory,
  });

  final AsyncValue<List<TransactionItem>> historyAsync;
  final VoidCallback onSeeAll;
  final VoidCallback onRetryHistory;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Transactions récentes',
              style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
            ),
            TextButton(onPressed: onSeeAll, child: const Text('Voir tout')),
          ],
        ),
        const SizedBox(height: 8),
        historyAsync.when(
          loading: () => const Card(
            child: Padding(
              padding: EdgeInsets.all(32),
              child: Center(child: CircularProgressIndicator()),
            ),
          ),
          error: (error, _) => Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: ErrorState(
                message: error is ApiException ? error.message : 'Impossible de charger l\'historique.',
                onRetry: onRetryHistory,
              ),
            ),
          ),
          data: (items) {
            if (items.isEmpty) {
              return const Card(
                child: Padding(
                  padding: EdgeInsets.all(24),
                  child: EmptyState(
                    message: 'Aucune transaction pour le moment.',
                    icon: Icons.receipt_long_outlined,
                  ),
                ),
              );
            }

            final recent = items.take(5).toList();
            return Card(
              clipBehavior: Clip.antiAlias,
              child: Column(
                children: [
                  for (var i = 0; i < recent.length; i++) ...[
                    TransactionTile(item: recent[i], compact: true),
                    if (i < recent.length - 1) const Divider(height: 1, indent: 56),
                  ],
                ],
              ),
            );
          },
        ),
      ],
    );
  }
}
