import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/features/wallet/data/wallet_repository.dart';
import 'package:minitransfer/features/wallet/domain/wallet_balance.dart';
import 'package:minitransfer/features/wallet/presentation/home_screen.dart';
import 'package:mocktail/mocktail.dart';

class _MockWalletRepository extends Mock implements WalletRepository {}

void main() {
  // L'écran affiche le solde formaté une fois chargé.
  testWidgets('affiche le solde formaté en FCFA', (tester) async {
    final repository = _MockWalletRepository();
    when(() => repository.getBalance())
        .thenAnswer((_) async => const WalletBalance(balance: 10000, currency: 'FCFA'));

    await tester.pumpWidget(
      ProviderScope(
        overrides: [walletRepositoryProvider.overrideWithValue(repository)],
        child: const MaterialApp(home: HomeScreen()),
      ),
    );

    // 1er frame : chargement ; puis la donnée arrive.
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 50));

    expect(find.text('Solde disponible'), findsOneWidget);
    expect(find.textContaining('FCFA'), findsWidgets);
    expect(find.text('Transférer'), findsOneWidget);
  });
}
