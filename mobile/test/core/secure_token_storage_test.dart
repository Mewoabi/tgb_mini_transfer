import 'dart:convert';

import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/storage/secure_token_storage.dart';
import 'package:minitransfer/features/auth/domain/user_model.dart';
import 'package:mocktail/mocktail.dart';

class _MockSecureStorage extends Mock implements FlutterSecureStorage {}

const _user = UserModel(
  id: '1',
  name: 'Alice',
  email: 'alice@example.com',
  phone: '+237600000001',
  balance: 10000,
);

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

  // saveUser persiste le profil en JSON.
  test('saveUser écrit le profil utilisateur', () async {
    when(() => secureStorage.write(key: any(named: 'key'), value: any(named: 'value')))
        .thenAnswer((_) async {});

    await tokenStorage.saveUser(_user);

    verify(() => secureStorage.write(
          key: 'auth_user',
          value: jsonEncode(_user.toJson()),
        )).called(1);
  });

  // readUser restaure le profil depuis le stockage.
  test('readUser renvoie le profil stocké', () async {
    when(() => secureStorage.read(key: 'auth_user'))
        .thenAnswer((_) async => jsonEncode(_user.toJson()));

    final user = await tokenStorage.readUser();

    expect(user?.name, _user.name);
    expect(user?.email, _user.email);
  });

  // clear supprime le token et le profil.
  test('clear supprime le token et le profil', () async {
    when(() => secureStorage.delete(key: any(named: 'key'))).thenAnswer((_) async {});

    await tokenStorage.clear();

    verify(() => secureStorage.delete(key: 'auth_token')).called(1);
    verify(() => secureStorage.delete(key: 'auth_user')).called(1);
  });
}

