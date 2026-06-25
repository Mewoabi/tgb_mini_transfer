import 'package:flutter/material.dart';

/// Écran affiché pendant la restauration de la session (état d'authentification inconnu).
class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(child: CircularProgressIndicator()),
    );
  }
}
