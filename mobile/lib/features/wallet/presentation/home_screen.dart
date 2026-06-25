import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/router/app_router.dart';
import '../../../core/utils/money.dart';
import '../../auth/application/auth_controller.dart';
import '../application/wallet_controller.dart';
import '../domain/wallet_balance.dart';

/// Écran d'accueil : solde du portefeuille, accès au transfert et déconnexion.
class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final balanceAsync = ref.watch(walletControllerProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('MiniTransfer'),
        actions: [
          IconButton(
            tooltip: 'Se déconnecter',
            icon: const Icon(Icons.logout),
            onPressed: () => ref.read(authControllerProvider.notifier).logout(),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(walletControllerProvider.notifier).refresh(),
        child: ListView(
          padding: const EdgeInsets.all(24),
          physics: const AlwaysScrollableScrollPhysics(),
          children: [
            balanceAsync.when(
              loading: () => const Padding(
                padding: EdgeInsets.symmetric(vertical: 120),
                child: Center(child: CircularProgressIndicator()),
              ),
              error: (error, _) => _ErrorView(
                message: error is ApiException ? error.message : 'Une erreur est survenue.',
                onRetry: () => ref.read(walletControllerProvider.notifier).refresh(),
              ),
              data: (balance) => _BalanceView(
                balance: balance,
                // push (et non go) : empile l'écran au-dessus de l'accueil,
                // ce qui fournit un bouton retour automatique.
                onTransfer: () => context.push(AppRoutes.transfer),
                onHistory: () => context.push(AppRoutes.history),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Carte de solde + bouton de transfert.
class _BalanceView extends StatelessWidget {
  const _BalanceView({
    required this.balance,
    required this.onTransfer,
    required this.onHistory,
  });

  final WalletBalance balance;
  final VoidCallback onTransfer;
  final VoidCallback onHistory;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        const SizedBox(height: 24),
        Card(
          color: theme.colorScheme.primaryContainer,
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 24),
            child: Column(
              children: [
                Text(
                  'Solde disponible',
                  style: theme.textTheme.titleMedium
                      ?.copyWith(color: theme.colorScheme.onPrimaryContainer),
                ),
                const SizedBox(height: 12),
                Text(
                  formatFcfa(balance.balance),
                  style: theme.textTheme.headlineMedium?.copyWith(
                    color: theme.colorScheme.onPrimaryContainer,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 32),
        FilledButton.icon(
          onPressed: onTransfer,
          icon: const Icon(Icons.send),
          label: const Text('Transférer'),
        ),
        const SizedBox(height: 12),
        OutlinedButton.icon(
          onPressed: onHistory,
          icon: const Icon(Icons.history),
          label: const Text('Historique'),
        ),
      ],
    );
  }
}

/// Affichage d'erreur avec bouton de réessai.
class _ErrorView extends StatelessWidget {
  const _ErrorView({required this.message, required this.onRetry});

  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 80),
      child: Column(
        children: [
          const Icon(Icons.error_outline, size: 48),
          const SizedBox(height: 16),
          Text(message, textAlign: TextAlign.center),
          const SizedBox(height: 16),
          OutlinedButton(onPressed: onRetry, child: const Text('Réessayer')),
        ],
      ),
    );
  }
}
