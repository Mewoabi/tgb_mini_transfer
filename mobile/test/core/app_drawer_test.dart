import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/presentation/app_drawer.dart';
import 'package:minitransfer/features/auth/application/current_user_controller.dart';
import 'package:minitransfer/features/auth/domain/user_model.dart';
import 'package:minitransfer/features/auth/application/auth_controller.dart';

class _TestAuthController extends AuthController {
  bool logoutCalled = false;

  @override
  AuthStatus build() => AuthStatus.authenticated;

  @override
  Future<void> logout() async {
    logoutCalled = true;
  }
}

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
  // Le tiroir affiche le nom, l'email et le bouton de déconnexion.
  testWidgets('affiche le profil et le bouton de déconnexion', (tester) async {
    final authController = _TestAuthController();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          currentUserProvider.overrideWith(_FakeCurrentUser.new),
          authControllerProvider.overrideWith(() => authController),
        ],
        child: MaterialApp(
          home: Scaffold(drawer: const AppDrawer()),
        ),
      ),
    );

    final scaffoldState = tester.firstState<ScaffoldState>(find.byType(Scaffold));
    scaffoldState.openDrawer();
    await tester.pumpAndSettle();

    expect(find.text('Alice Martin'), findsOneWidget);
    expect(find.text('alice@example.com'), findsOneWidget);
    expect(find.text('Déconnexion'), findsOneWidget);

    await tester.tap(find.text('Déconnexion'));
    await tester.pumpAndSettle();

    expect(authController.logoutCalled, isTrue);
  });
}
