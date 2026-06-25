import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// Stockage sécurisé du token JWT (Keystore Android / Keychain iOS).
class SecureTokenStorage {
  SecureTokenStorage(this._storage);

  static const String _tokenKey = 'auth_token';
  final FlutterSecureStorage _storage;

  Future<void> saveToken(String token) => _storage.write(key: _tokenKey, value: token);

  Future<String?> readToken() => _storage.read(key: _tokenKey);

  Future<void> clear() => _storage.delete(key: _tokenKey);
}

/// Instance bas niveau de flutter_secure_storage.
final secureStorageProvider = Provider<FlutterSecureStorage>(
  (ref) => const FlutterSecureStorage(),
);

/// Stockage du token de l'application, construit au-dessus de [secureStorageProvider].
final tokenStorageProvider = Provider<SecureTokenStorage>(
  (ref) => SecureTokenStorage(ref.read(secureStorageProvider)),
);
