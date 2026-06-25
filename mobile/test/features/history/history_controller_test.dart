import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/network/api_exception.dart';
import 'package:minitransfer/features/history/application/history_controller.dart';
import 'package:minitransfer/features/history/data/history_repository.dart';
import 'package:minitransfer/features/history/domain/transaction_item.dart';
import 'package:mocktail/mocktail.dart';

import '../../helpers/auth_test_helpers.dart';

class _MockHistoryRepository extends Mock implements HistoryRepository {}

final _items = [
  TransactionItem(
    id: '1',
    direction: TransactionDirection.sent,
    counterpartyName: 'Bob',
    amount: 3000,
    timestamp: DateTime(2026, 1, 2, 10),
    status: 'COMPLETED',
  ),
];

void main() {
  late _MockHistoryRepository repository;
  late ProviderContainer container;

  setUp(() {
    repository = _MockHistoryRepository();
    container = ProviderContainer(
      overrides: [
        historyRepositoryProvider.overrideWithValue(repository),
        authenticatedAuthOverride,
      ],
    );
    addTearDown(container.dispose);
  });

  test('charge l\'historique depuis le dépôt', () async {
    when(() => repository.getHistory()).thenAnswer((_) async => _items);

    final list = await container.read(historyControllerProvider.future);

    expect(list, hasLength(1));
    expect(list.first.counterpartyName, 'Bob');
  });

  test('expose une erreur si le chargement échoue', () async {
    when(() => repository.getHistory())
        .thenAnswer((_) async => throw const ApiException('Erreur serveur.', statusCode: 500));

    container.listen(historyControllerProvider, (_, _) {}, fireImmediately: true);
    await Future<void>.delayed(const Duration(milliseconds: 100));

    final state = container.read(historyControllerProvider);
    expect(state.hasError, isTrue);
    expect(state.error, isA<ApiException>());
  });

  test('refresh recharge l\'historique', () async {
    when(() => repository.getHistory()).thenAnswer((_) async => <TransactionItem>[]);
    await container.read(historyControllerProvider.future);

    when(() => repository.getHistory()).thenAnswer((_) async => _items);
    await container.read(historyControllerProvider.notifier).refresh();

    expect(container.read(historyControllerProvider).value, hasLength(1));
  });
}
