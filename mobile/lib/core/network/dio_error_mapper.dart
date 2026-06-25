import 'package:dio/dio.dart';

import 'api_exception.dart';

/// Traduit une [DioException] en [ApiException] avec un message convivial en français.
///
/// Privilégie le message renvoyé par l'API (corps `{ message, fieldErrors }`) et retombe
/// sur un message générique selon le code HTTP ou le type d'erreur réseau.
ApiException mapDioError(DioException error) {
  switch (error.type) {
    case DioExceptionType.connectionTimeout:
    case DioExceptionType.sendTimeout:
    case DioExceptionType.receiveTimeout:
      return const ApiException('Délai de connexion dépassé. Vérifiez votre connexion.');
    case DioExceptionType.connectionError:
      return const ApiException('Impossible de joindre le serveur. Vérifiez votre connexion.');
    default:
      break;
  }

  final statusCode = error.response?.statusCode;
  final serverMessage = _extractMessage(error.response?.data);

  final message = switch (statusCode) {
    400 => serverMessage ?? 'Requête invalide.',
    401 => serverMessage ?? 'Identifiants invalides ou session expirée.',
    404 => serverMessage ?? 'Ressource introuvable.',
    409 => serverMessage ?? 'Conflit avec l\'état actuel de la ressource.',
    _ => serverMessage ?? 'Une erreur est survenue. Veuillez réessayer.',
  };

  return ApiException(message, statusCode: statusCode);
}

/// Extrait un message lisible du corps d'erreur de l'API (champ `message`, ou `fieldErrors`).
String? _extractMessage(dynamic data) {
  if (data is Map) {
    final fieldErrors = data['fieldErrors'];
    if (fieldErrors is Map && fieldErrors.isNotEmpty) {
      final details = fieldErrors.values.whereType<String>().join('\n');
      if (details.isNotEmpty) return details;
    }
    final message = data['message'];
    if (message is String && message.isNotEmpty) return message;
  }
  return null;
}
