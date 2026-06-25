import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/network/api_exception.dart';
import 'package:minitransfer/features/wallet/application/wallet_controller.dart';
import 'package:minitransfer/features/wallet/data/wallet_repository.dart';
import 'package:minitransfer/features/wallet/domain/wallet_balance.dart';
import 'package:mocktail/mocktail.dart';

import '../../helpers/auth_test_helpers.dart';

class _MockWalletRepository extends Mock implements WalletRepository {}

void main() {
  late _MockWalletRepository repository;
  late ProviderContainer container;

  setUp(() {
    repository = _MockWalletRepository();
    container = ProviderContainer(
      overrides: [
        walletRepositoryProvider.overrideWithValue(repository),
        authenticatedAuthOverride,
      ],
    );
    addTearDown(container.dispose);
  });

  test('charge le solde depuis le dépôt', () async {
    when(() => repository.getBalance())
        .thenAnswer((_) async => const WalletBalance(balance: 10000, currency: 'FCFA'));

    final balance = await container.read(walletControllerProvider.future);

    expect(balance.balance, 10000);
    expect(balance.currency, 'FCFA');
  });

  test('expose une erreur si le chargement échoue', () async {
    when(() => repository.getBalance())
        .thenAnswer((_) async => throw const ApiException('Erreur serveur.', statusCode: 500));

    // On écoute le provider pour le maintenir actif, puis on laisse le build échouer.
    container.listen(walletControllerProvider, (_, _) {}, fireImmediately: true);
    await Future<void>.delayed(const Duration(milliseconds: 100));

    final state = container.read(walletControllerProvider);
    expect(state.hasError, isTrue);
    expect(state.error, isA<ApiException>());
  });

  test('refresh recharge le solde', () async {
    when(() => repository.getBalance())
        .thenAnswer((_) async => const WalletBalance(balance: 5000, currency: 'FCFA'));
    await container.read(walletControllerProvider.future);

    when(() => repository.getBalance())
        .thenAnswer((_) async => const WalletBalance(balance: 7000, currency: 'FCFA'));
    await container.read(walletControllerProvider.notifier).refresh();

    expect(container.read(walletControllerProvider).value?.balance, 7000);
  });
}
