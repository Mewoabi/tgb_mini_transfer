import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/app.dart';

void main() {
  // Vérifie que l'application démarre et affiche son nom (test de fumée de l'échafaudage).
  testWidgets('affiche le nom de l\'application au démarrage', (tester) async {
    await tester.pumpWidget(const ProviderScope(child: MiniTransferApp()));

    expect(find.text('MiniTransfer'), findsWidgets);
  });
}
