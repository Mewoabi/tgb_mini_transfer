/// Représentation de l'utilisateur connecté (vue renvoyée par l'API).
class UserModel {
  const UserModel({
    required this.id,
    required this.name,
    required this.email,
    required this.phone,
    required this.balance,
  });

  final String id;
  final String name;
  final String email;
  final String phone;

  /// Solde du portefeuille, en FCFA (entier).
  final int balance;

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'] as String,
      name: json['name'] as String,
      email: json['email'] as String,
      phone: json['phone'] as String,
      balance: (json['balance'] as num).toInt(),
    );
  }
}
