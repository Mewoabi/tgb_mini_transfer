import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minitransfer/features/auth/application/auth_controller.dart';

/// Contrôleur d'authentification figé sur « connecté » pour les tests unitaires.
class AuthenticatedAuthController extends AuthController {
  @override
  AuthStatus build() => AuthStatus.authenticated;
}

/// Surcharge [authControllerProvider] pour simuler une session active.
final authenticatedAuthOverride = authControllerProvider.overrideWith(
  AuthenticatedAuthController.new,
);
