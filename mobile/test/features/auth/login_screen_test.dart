import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/features/auth/presentation/login_screen.dart';

void main() {
  // Soumettre le formulaire vide affiche les messages de validation, sans appel réseau.
  testWidgets('affiche les erreurs de validation si le formulaire est vide', (tester) async {
    await tester.pumpWidget(
      const ProviderScope(child: MaterialApp(home: LoginScreen())),
    );

    await tester.tap(find.text('Se connecter'));
    await tester.pump();

    expect(find.text("L'email est obligatoire."), findsOneWidget);
    expect(find.text('Le mot de passe est obligatoire.'), findsOneWidget);
  });
}
