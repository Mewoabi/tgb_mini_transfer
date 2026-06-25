/// Configuration globale de l'application.
class AppConfig {
  const AppConfig._();

  /// URL de base de l'API REST.
  ///
  /// Surchargeable au build via `--dart-define=API_BASE_URL=...`.
  /// Valeur par défaut : `10.0.2.2:8080`, qui désigne la machine hôte vue depuis
  /// l'émulateur Android (sur un appareil physique, utiliser l'IP locale de la machine).
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );
}
