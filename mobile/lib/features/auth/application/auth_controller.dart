import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_token_storage.dart';
import 'current_user_controller.dart';
import '../data/auth_repository.dart';

/// État d'authentification de la session.
///
/// `unknown` : restauration en cours (au démarrage) ; `authenticated` : token présent ;
/// `unauthenticated` : aucun token.
enum AuthStatus { unknown, authenticated, unauthenticated }

/// Contrôleur de session : restaure le token au démarrage, gère l'inscription, la connexion
/// et la déconnexion. En cas de succès, le token est persisté et l'état passe à `authenticated`,
/// ce qui déclenche automatiquement la redirection du routeur vers l'accueil.
///
/// Les méthodes [login] et [register] propagent une `ApiException` en cas d'échec ;
/// les écrans l'attrapent pour afficher un message convivial.
class AuthController extends Notifier<AuthStatus> {
  late final SecureTokenStorage _tokenStorage;
  late final AuthRepository _repository;

  @override
  AuthStatus build() {
    _tokenStorage = ref.read(tokenStorageProvider);
    _repository = ref.read(authRepositoryProvider);
    _restoreSession();
    return AuthStatus.unknown;
  }

  Future<void> _restoreSession() async {
    final token = await _tokenStorage.readToken();
    state = token != null ? AuthStatus.authenticated : AuthStatus.unauthenticated;
  }

  Future<void> register({
    required String name,
    required String email,
    required String phone,
    required String password,
  }) async {
    final result = await _repository.register(
      name: name,
      email: email,
      phone: phone,
      password: password,
    );
    await _tokenStorage.saveToken(result.token);
    await ref.read(currentUserProvider.notifier).setUser(result.user);
    state = AuthStatus.authenticated;
  }

  Future<void> login({required String email, required String password}) async {
    final result = await _repository.login(email: email, password: password);
    await _tokenStorage.saveToken(result.token);
    await ref.read(currentUserProvider.notifier).setUser(result.user);
    state = AuthStatus.authenticated;
  }

  /// Déconnexion : efface le token et repasse en état non authentifié.
  Future<void> logout() async {
    await _tokenStorage.clear();
    await ref.read(currentUserProvider.notifier).clearUser();
    state = AuthStatus.unauthenticated;
  }
}

final authControllerProvider = NotifierProvider<AuthController, AuthStatus>(
  AuthController.new,
);
