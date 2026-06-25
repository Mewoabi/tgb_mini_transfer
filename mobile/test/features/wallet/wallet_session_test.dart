import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/features/auth/application/auth_controller.dart';
import 'package:minitransfer/features/wallet/application/wallet_controller.dart';
import 'package:minitransfer/features/wallet/data/wallet_repository.dart';
import 'package:minitransfer/features/wallet/domain/wallet_balance.dart';
import 'package:mocktail/mocktail.dart';

class _MockWalletRepository extends Mock implements WalletRepository {}

class _UnauthenticatedAuth extends AuthController {
  @override
  AuthStatus build() => AuthStatus.unauthenticated;
}

void main() {
  test('ne charge pas le solde sans session active', () async {
    final walletRepository = _MockWalletRepository();
    when(() => walletRepository.getBalance())
        .thenAnswer((_) async => const WalletBalance(balance: 10000, currency: 'FCFA'));

    final container = ProviderContainer(
      overrides: [
        walletRepositoryProvider.overrideWithValue(walletRepository),
        authControllerProvider.overrideWith(_UnauthenticatedAuth.new),
      ],
    );
    addTearDown(container.dispose);

    final balance = await container.read(walletControllerProvider.future);

    expect(balance.balance, 0);
    verifyNever(() => walletRepository.getBalance());
  });
}
