import 'user_model.dart';

/// Résultat d'une authentification : token JWT + informations de l'utilisateur.
class AuthResult {
  const AuthResult({required this.token, required this.user});

  final String token;
  final UserModel user;

  factory AuthResult.fromJson(Map<String, dynamic> json) {
    return AuthResult(
      token: json['token'] as String,
      user: UserModel.fromJson(json['user'] as Map<String, dynamic>),
    );
  }
}
