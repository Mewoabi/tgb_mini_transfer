import 'package:flutter/material.dart';

/// Écran d'accueil (provisoire — le solde et les actions sont ajoutés à l'étape suivante).
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Accueil')),
      body: const Center(child: Text('Accueil (à venir)')),
    );
  }
}
