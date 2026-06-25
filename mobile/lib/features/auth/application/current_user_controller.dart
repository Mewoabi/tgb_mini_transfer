import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/storage/secure_token_storage.dart';
import '../domain/user_model.dart';

/// Profil de l'utilisateur connecté, restauré depuis le stockage sécurisé.
///
/// `null` tant que la restauration est en cours ou si aucun profil n'est persisté
/// (ex. session créée avant la persistance du profil).
class CurrentUserController extends Notifier<UserModel?> {
  late final SecureTokenStorage _tokenStorage;

  @override
  UserModel? build() {
    _tokenStorage = ref.read(tokenStorageProvider);
    _restoreUser();
    return null;
  }

  Future<void> _restoreUser() async {
    final user = await _tokenStorage.readUser();
    state = user;
  }

  Future<void> setUser(UserModel user) async {
    await _tokenStorage.saveUser(user);
    state = user;
  }

  Future<void> clearUser() async {
    state = null;
  }
}

final currentUserProvider = NotifierProvider<CurrentUserController, UserModel?>(
  CurrentUserController.new,
);
