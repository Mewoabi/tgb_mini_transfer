import 'package:flutter/material.dart';

/// Écran de transfert (provisoire — le formulaire réel est ajouté à l'étape suivante).
class TransferScreen extends StatelessWidget {
  const TransferScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Transfert')),
      body: const Center(child: Text('Formulaire de transfert (à venir)')),
    );
  }
}
