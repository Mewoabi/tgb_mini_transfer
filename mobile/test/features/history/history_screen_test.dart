import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/features/history/data/history_repository.dart';
import 'package:minitransfer/features/history/domain/transaction_item.dart';
import 'package:minitransfer/features/history/presentation/history_screen.dart';
import 'package:mocktail/mocktail.dart';

import '../../helpers/auth_test_helpers.dart';

class _MockHistoryRepository extends Mock implements HistoryRepository {}

void main() {
  // L'écran affiche les transactions émises et reçues une fois chargées.
  testWidgets('affiche les transactions émises et reçues', (tester) async {
    final repository = _MockHistoryRepository();
    when(() => repository.getHistory()).thenAnswer(
      (_) async => [
        TransactionItem(
          id: '1',
          direction: TransactionDirection.sent,
          counterpartyName: 'Bob',
          amount: 3000,
          timestamp: DateTime(2026, 1, 2, 10),
          status: 'COMPLETED',
        ),
        TransactionItem(
          id: '2',
          direction: TransactionDirection.received,
          counterpartyName: 'Carol',
          amount: 1500,
          timestamp: DateTime(2026, 1, 1, 9),
          status: 'COMPLETED',
        ),
      ],
    );

    await tester.pumpWidget(
      ProviderScope(
        overrides: [historyRepositoryProvider.overrideWithValue(repository), authenticatedAuthOverride],
        child: const MaterialApp(
          home: Scaffold(body: HistoryScreen()),
        ),
      ),
    );

    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(find.text('Bob'), findsOneWidget);
    expect(find.text('Carol'), findsOneWidget);
    expect(find.textContaining('- '), findsOneWidget);
    expect(find.textContaining('+ '), findsOneWidget);
  });

  // Un utilisateur sans transaction voit l'état vide.
  testWidgets('affiche l\'état vide en l\'absence de transaction', (tester) async {
    final repository = _MockHistoryRepository();
    when(() => repository.getHistory()).thenAnswer((_) async => <TransactionItem>[]);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [historyRepositoryProvider.overrideWithValue(repository), authenticatedAuthOverride],
        child: const MaterialApp(
          home: Scaffold(body: HistoryScreen()),
        ),
      ),
    );

    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(find.text('Aucune transaction pour le moment.'), findsOneWidget);
  });

  // Le filtre « Émis » masque les transactions reçues.
  testWidgets('filtre les transactions émises uniquement', (tester) async {
    final repository = _MockHistoryRepository();
    when(() => repository.getHistory()).thenAnswer(
      (_) async => [
        TransactionItem(
          id: '1',
          direction: TransactionDirection.sent,
          counterpartyName: 'Bob',
          amount: 3000,
          timestamp: DateTime(2026, 1, 2, 10),
          status: 'COMPLETED',
        ),
        TransactionItem(
          id: '2',
          direction: TransactionDirection.received,
          counterpartyName: 'Carol',
          amount: 1500,
          timestamp: DateTime(2026, 1, 1, 9),
          status: 'COMPLETED',
        ),
      ],
    );

    await tester.pumpWidget(
      ProviderScope(
        overrides: [historyRepositoryProvider.overrideWithValue(repository), authenticatedAuthOverride],
        child: const MaterialApp(
          home: Scaffold(body: HistoryScreen()),
        ),
      ),
    );

    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(find.text('Bob'), findsOneWidget);
    expect(find.text('Carol'), findsOneWidget);

    await tester.tap(find.text('Émis'));
    await tester.pumpAndSettle();

    expect(find.text('Bob'), findsOneWidget);
    expect(find.text('Carol'), findsNothing);
  });
}
