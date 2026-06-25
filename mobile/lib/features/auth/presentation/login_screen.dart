import 'package:flutter/material.dart';

/// Écran de connexion (provisoire — le formulaire réel est ajouté à l'étape suivante).
class LoginScreen extends StatelessWidget {
  const LoginScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Connexion')),
      body: const Center(child: Text('Écran de connexion (à venir)')),
    );
  }
}
