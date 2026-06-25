import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/network/api_exception.dart';
import 'package:minitransfer/core/storage/secure_token_storage.dart';
import 'package:minitransfer/features/auth/application/auth_controller.dart';
import 'package:minitransfer/features/auth/data/auth_repository.dart';
import 'package:minitransfer/features/auth/domain/auth_result.dart';
import 'package:minitransfer/features/auth/domain/user_model.dart';
import 'package:mocktail/mocktail.dart';

class _MockAuthRepository extends Mock implements AuthRepository {}

class _MockSecureStorage extends Mock implements FlutterSecureStorage {}

const _user = UserModel(
  id: '1',
  name: 'Alice',
  email: 'alice@example.com',
  phone: '+237600000001',
  balance: 10000,
);

void main() {
  late _MockAuthRepository repository;
  late _MockSecureStorage secureStorage;
  late ProviderContainer container;

  setUp(() {
    repository = _MockAuthRepository();
    secureStorage = _MockSecureStorage();
    // Aucun token au démarrage (restauration -> unauthenticated).
    when(() => secureStorage.read(key: any(named: 'key'))).thenAnswer((_) async => null);
    when(() => secureStorage.write(key: any(named: 'key'), value: any(named: 'value')))
        .thenAnswer((_) async {});
    when(() => secureStorage.delete(key: any(named: 'key'))).thenAnswer((_) async {});

    container = ProviderContainer(overrides: [
      authRepositoryProvider.overrideWithValue(repository),
      secureStorageProvider.overrideWithValue(secureStorage),
    ]);
    addTearDown(container.dispose);
  });

  // Laisse la restauration de session (lecture du token) se terminer.
  Future<void> settleRestore() => Future<void>.delayed(Duration.zero);

  test('login réussi enregistre le token et passe à authenticated', () async {
    when(() => repository.login(email: any(named: 'email'), password: any(named: 'password')))
        .thenAnswer((_) async => const AuthResult(token: 'tok', user: _user));

    final controller = container.read(authControllerProvider.notifier);
    await settleRestore();

    await controller.login(email: 'alice@example.com', password: 'password123');

    expect(container.read(authControllerProvider), AuthStatus.authenticated);
    verify(() => secureStorage.write(key: 'auth_token', value: 'tok')).called(1);
    verify(() => secureStorage.write(
          key: 'auth_user',
          value: any(named: 'value'),
        )).called(1);
  });

  test('login en échec propage l\'erreur et reste non authentifié', () async {
    when(() => repository.login(email: any(named: 'email'), password: any(named: 'password')))
        .thenThrow(const ApiException('Identifiants invalides.', statusCode: 401));

    final controller = container.read(authControllerProvider.notifier);
    await settleRestore();

    await expectLater(
      controller.login(email: 'alice@example.com', password: 'mauvais'),
      throwsA(isA<ApiException>()),
    );
    expect(container.read(authControllerProvider), AuthStatus.unauthenticated);
    verifyNever(() => secureStorage.write(key: any(named: 'key'), value: any(named: 'value')));
  });

  test('register réussi enregistre le token et passe à authenticated', () async {
    when(() => repository.register(
          name: any(named: 'name'),
          email: any(named: 'email'),
          phone: any(named: 'phone'),
          password: any(named: 'password'),
        )).thenAnswer((_) async => const AuthResult(token: 'tok2', user: _user));

    final controller = container.read(authControllerProvider.notifier);
    await settleRestore();

    await controller.register(
      name: 'Alice',
      email: 'alice@example.com',
      phone: '+237600000001',
      password: 'password123',
    );

    expect(container.read(authControllerProvider), AuthStatus.authenticated);
    verify(() => secureStorage.write(key: 'auth_token', value: 'tok2')).called(1);
    verify(() => secureStorage.write(
          key: 'auth_user',
          value: any(named: 'value'),
        )).called(1);
  });

  test('logout efface le token et repasse en non authentifié', () async {
    final controller = container.read(authControllerProvider.notifier);
    await settleRestore();

    await controller.logout();

    expect(container.read(authControllerProvider), AuthStatus.unauthenticated);
    verify(() => secureStorage.delete(key: 'auth_token')).called(1);
    verify(() => secureStorage.delete(key: 'auth_user')).called(1);
  });
}
