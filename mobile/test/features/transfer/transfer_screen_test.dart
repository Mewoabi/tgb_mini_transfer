import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/features/transfer/presentation/transfer_screen.dart';

void main() {
  // Soumettre le formulaire vide affiche les messages de validation.
  testWidgets('affiche les erreurs de validation si le formulaire est vide', (tester) async {
    await tester.pumpWidget(
      const ProviderScope(child: MaterialApp(home: TransferScreen())),
    );

    await tester.tap(find.text('Envoyer'));
    await tester.pump();

    expect(find.text('Le destinataire est obligatoire.'), findsOneWidget);
    expect(find.text('Le montant est obligatoire.'), findsOneWidget);
  });
}
