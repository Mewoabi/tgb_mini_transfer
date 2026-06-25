import 'package:flutter/material.dart';

import 'core/config/app_config.dart';
import 'core/theme/app_theme.dart';

/// Application racine MiniTransfer.
class MiniTransferApp extends StatelessWidget {
  const MiniTransferApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MiniTransfer',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light(),
      home: const _StartupPlaceholder(),
    );
  }
}

/// Écran provisoire affiché à l'étape d'échafaudage.
/// Il sera remplacé par la navigation réelle (connexion / accueil) aux étapes suivantes.
class _StartupPlaceholder extends StatelessWidget {
  const _StartupPlaceholder();

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('MiniTransfer')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.account_balance_wallet_outlined, size: 72),
            const SizedBox(height: 16),
            Text('MiniTransfer', style: textTheme.headlineSmall),
            const SizedBox(height: 8),
            Text(
              'Transfert d\'argent simplifié',
              style: textTheme.bodyMedium,
            ),
            const SizedBox(height: 24),
            Text('API : ${AppConfig.apiBaseUrl}', style: textTheme.bodySmall),
          ],
        ),
      ),
    );
  }
}
