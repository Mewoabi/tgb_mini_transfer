/// Validateurs de formulaire réutilisables (messages en français).
class Validators {
  const Validators._();

  static String? notEmpty(String? value, String message) {
    return (value == null || value.trim().isEmpty) ? message : null;
  }

  static String? email(String? value) {
    if (value == null || value.trim().isEmpty) {
      return "L'email est obligatoire.";
    }
    final regex = RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$');
    return regex.hasMatch(value.trim()) ? null : 'Email invalide.';
  }

  static String? phone(String? value) {
    if (value == null || value.trim().isEmpty) {
      return 'Le numéro de téléphone est obligatoire.';
    }
    final regex = RegExp(r'^\+?[0-9]{8,15}$');
    return regex.hasMatch(value.trim()) ? null : 'Numéro invalide (8 à 15 chiffres).';
  }

  static String? password(String? value) {
    if (value == null || value.isEmpty) {
      return 'Le mot de passe est obligatoire.';
    }
    return value.length < 8 ? 'Au moins 8 caractères.' : null;
  }
}
