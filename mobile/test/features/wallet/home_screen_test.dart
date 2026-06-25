import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/features/history/data/history_repository.dart';
import 'package:minitransfer/features/history/domain/transaction_item.dart';
import 'package:minitransfer/features/wallet/data/wallet_repository.dart';
import 'package:minitransfer/features/wallet/domain/wallet_balance.dart';
import 'package:minitransfer/features/wallet/presentation/home_screen.dart';
import 'package:minitransfer/features/auth/application/current_user_controller.dart';
import 'package:minitransfer/features/auth/domain/user_model.dart';
import 'package:mocktail/mocktail.dart';

import '../../helpers/auth_test_helpers.dart';

class _MockWalletRepository extends Mock implements WalletRepository {}

class _MockHistoryRepository extends Mock implements HistoryRepository {}

class _FakeCurrentUser extends CurrentUserController {
  @override
  UserModel? build() => _user;
}

const _user = UserModel(
  id: '1',
  name: 'Alice Martin',
  email: 'alice@example.com',
  phone: '+237600000001',
  balance: 10000,
);

void main() {
  // Le tableau de bord affiche la salutation, le solde et les transactions récentes.
  testWidgets('affiche la salutation, le solde et les transactions récentes', (tester) async {
    final walletRepository = _MockWalletRepository();
    final historyRepository = _MockHistoryRepository();

    when(() => walletRepository.getBalance())
        .thenAnswer((_) async => const WalletBalance(balance: 10000, currency: 'FCFA'));
    when(() => historyRepository.getHistory()).thenAnswer(
      (_) async => [
        TransactionItem(
          id: '1',
          direction: TransactionDirection.sent,
          counterpartyName: 'Bob',
          amount: 3000,
          timestamp: DateTime(2026, 1, 2, 10),
          status: 'COMPLETED',
        ),
      ],
    );

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          walletRepositoryProvider.overrideWithValue(walletRepository),
          historyRepositoryProvider.overrideWithValue(historyRepository),
          currentUserProvider.overrideWith(_FakeCurrentUser.new),
          authenticatedAuthOverride,
        ],
        child: const MaterialApp(home: HomeScreen()),
      ),
    );

    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(find.textContaining('Bonjour, Alice'), findsOneWidget);
    expect(find.text('Solde disponible'), findsOneWidget);
    expect(find.textContaining('FCFA'), findsWidgets);
    expect(find.text('Transactions récentes'), findsOneWidget);
    expect(find.text('Bob'), findsOneWidget);
    expect(find.text('Total envoyé'), findsOneWidget);
    expect(find.text('Total reçu'), findsOneWidget);
  });
}
