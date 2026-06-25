/// Utilitaires d'affichage convivial (salutation, prénom).
class GreetingUtils {
  const GreetingUtils._();

  /// Extrait le prénom (premier mot) d'un nom complet.
  static String firstName(String fullName) {
    final trimmed = fullName.trim();
    if (trimmed.isEmpty) return 'Utilisateur';
    return trimmed.split(RegExp(r'\s+')).first;
  }
}
