import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_token_storage.dart';

/// État d'authentification de la session.
///
/// `unknown` : restauration en cours (au démarrage) ; `authenticated` : token présent ;
/// `unauthenticated` : aucun token.
enum AuthStatus { unknown, authenticated, unauthenticated }

/// Contrôleur de session : restaure le token au démarrage et gère la déconnexion.
///
/// L'inscription et la connexion (qui enregistrent le token et passent à `authenticated`)
/// seront ajoutées à l'étape des écrans d'authentification.
class AuthController extends Notifier<AuthStatus> {
  late final SecureTokenStorage _tokenStorage;

  @override
  AuthStatus build() {
    _tokenStorage = ref.read(tokenStorageProvider);
    _restoreSession();
    return AuthStatus.unknown;
  }

  Future<void> _restoreSession() async {
    final token = await _tokenStorage.readToken();
    state = token != null ? AuthStatus.authenticated : AuthStatus.unauthenticated;
  }

  /// Déconnexion : efface le token et repasse en état non authentifié.
  Future<void> logout() async {
    await _tokenStorage.clear();
    state = AuthStatus.unauthenticated;
  }
}

final authControllerProvider = NotifierProvider<AuthController, AuthStatus>(
  AuthController.new,
);
