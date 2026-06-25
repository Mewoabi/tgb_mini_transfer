import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/storage/secure_token_storage.dart';
import 'package:mocktail/mocktail.dart';

class _MockSecureStorage extends Mock implements FlutterSecureStorage {}

void main() {
  late _MockSecureStorage secureStorage;
  late SecureTokenStorage tokenStorage;

  setUp(() {
    secureStorage = _MockSecureStorage();
    tokenStorage = SecureTokenStorage(secureStorage);
  });

  // saveToken écrit le token sous la bonne clé.
  test('saveToken écrit le token', () async {
    when(() => secureStorage.write(key: any(named: 'key'), value: any(named: 'value')))
        .thenAnswer((_) async {});

    await tokenStorage.saveToken('abc');

    verify(() => secureStorage.write(key: 'auth_token', value: 'abc')).called(1);
  });

  // readToken renvoie la valeur stockée.
  test('readToken renvoie le token stocké', () async {
    when(() => secureStorage.read(key: any(named: 'key'))).thenAnswer((_) async => 'abc');

    expect(await tokenStorage.readToken(), 'abc');
  });

  // clear supprime le token.
  test('clear supprime le token', () async {
    when(() => secureStorage.delete(key: any(named: 'key'))).thenAnswer((_) async {});

    await tokenStorage.clear();

    verify(() => secureStorage.delete(key: 'auth_token')).called(1);
  });
}
