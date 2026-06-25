import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/history_repository.dart';
import '../domain/transaction_item.dart';

/// Contrôleur de l'historique : charge la liste des transactions et permet de la rafraîchir.
class HistoryController extends AsyncNotifier<List<TransactionItem>> {
  @override
  Future<List<TransactionItem>> build() {
    return ref.read(historyRepositoryProvider).getHistory();
  }

  Future<void> refresh() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() => ref.read(historyRepositoryProvider).getHistory());
  }
}

final historyControllerProvider =
    AsyncNotifierProvider<HistoryController, List<TransactionItem>>(
  HistoryController.new,
);
