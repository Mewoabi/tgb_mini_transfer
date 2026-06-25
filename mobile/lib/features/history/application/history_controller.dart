import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../auth/application/auth_controller.dart';
import '../data/history_repository.dart';
import '../domain/transaction_item.dart';

/// Contrôleur de l'historique : charge la liste des transactions et permet de la rafraîchir.
/// Reconstruit automatiquement lors d'un changement de session (connexion / déconnexion).
class HistoryController extends AsyncNotifier<List<TransactionItem>> {
  @override
  Future<List<TransactionItem>> build() {
    final status = ref.watch(authControllerProvider);
    if (status != AuthStatus.authenticated) {
      return Future.value([]);
    }
    return ref.read(historyRepositoryProvider).getHistory();
  }

  Future<void> refresh() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() => ref.read(historyRepositoryProvider).getHistory());
  }
}

final historyControllerProvider =
    AsyncNotifierProvider.autoDispose<HistoryController, List<TransactionItem>>(
  HistoryController.new,
);
