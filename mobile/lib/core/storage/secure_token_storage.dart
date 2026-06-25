import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../../features/auth/domain/user_model.dart';

/// Stockage sécurisé du token JWT et du profil utilisateur (Keystore Android / Keychain iOS).
class SecureTokenStorage {
  SecureTokenStorage(this._storage);

  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'auth_user';
  final FlutterSecureStorage _storage;

  Future<void> saveToken(String token) => _storage.write(key: _tokenKey, value: token);

  Future<String?> readToken() => _storage.read(key: _tokenKey);

  Future<void> saveUser(UserModel user) =>
      _storage.write(key: _userKey, value: jsonEncode(user.toJson()));

  Future<UserModel?> readUser() async {
    final raw = await _storage.read(key: _userKey);
    if (raw == null) return null;
    return UserModel.fromJson(jsonDecode(raw) as Map<String, dynamic>);
  }

  Future<void> clear() async {
    await _storage.delete(key: _tokenKey);
    await _storage.delete(key: _userKey);
  }
}

/// Instance bas niveau de flutter_secure_storage.
final secureStorageProvider = Provider<FlutterSecureStorage>(
  (ref) => const FlutterSecureStorage(),
);

/// Stockage du token de l'application, construit au-dessus de [secureStorageProvider].
final tokenStorageProvider = Provider<SecureTokenStorage>(
  (ref) => SecureTokenStorage(ref.read(secureStorageProvider)),
);
