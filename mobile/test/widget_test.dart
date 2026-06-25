import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/app.dart';
import 'package:minitransfer/core/storage/secure_token_storage.dart';
import 'package:mocktail/mocktail.dart';

class _MockSecureStorage extends Mock implements FlutterSecureStorage {}

void main() {
  // Sans token stocké, la garde d'authentification doit rediriger vers l'écran de connexion.
  testWidgets('sans token, l\'application affiche l\'écran de connexion', (tester) async {
    final secureStorage = _MockSecureStorage();
    when(() => secureStorage.read(key: any(named: 'key'))).thenAnswer((_) async => null);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [secureStorageProvider.overrideWithValue(secureStorage)],
        child: const MiniTransferApp(),
      ),
    );

    // Laisse la restauration de session s'exécuter puis le routeur rediriger.
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 100));

    expect(find.text('Connexion'), findsWidgets);
  });
}
