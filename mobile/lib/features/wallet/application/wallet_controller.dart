import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/wallet_repository.dart';
import '../domain/wallet_balance.dart';

/// Contrôleur du solde : charge le solde au premier accès et permet de le rafraîchir.
///
/// L'état est un [AsyncValue] : l'UI gère uniformément le chargement, l'erreur et la donnée.
class WalletController extends AsyncNotifier<WalletBalance> {
  @override
  Future<WalletBalance> build() {
    return ref.read(walletRepositoryProvider).getBalance();
  }

  /// Recharge le solde (utilisé par le « pull-to-refresh » et après un transfert).
  Future<void> refresh() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() => ref.read(walletRepositoryProvider).getBalance());
  }
}

final walletControllerProvider = AsyncNotifierProvider<WalletController, WalletBalance>(
  WalletController.new,
);
