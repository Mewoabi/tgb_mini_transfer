import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/network/dio_error_mapper.dart';

void main() {
  RequestOptions options() => RequestOptions(path: '/test');

  DioException withResponse(int status, dynamic data) => DioException(
        requestOptions: options(),
        type: DioExceptionType.badResponse,
        response: Response(requestOptions: options(), statusCode: status, data: data),
      );

  // Un dépassement de délai donne un message réseau convivial.
  test('mappe un timeout en message réseau', () {
    final exception = mapDioError(
      DioException(requestOptions: options(), type: DioExceptionType.connectionTimeout),
    );
    expect(exception.message, contains('Délai'));
  });

  // Une erreur de connexion donne un message « serveur injoignable ».
  test('mappe une erreur de connexion', () {
    final exception = mapDioError(
      DioException(requestOptions: options(), type: DioExceptionType.connectionError),
    );
    expect(exception.message, contains('serveur'));
  });

  // Le message du serveur est repris pour un 401.
  test('mappe 401 avec le message du serveur', () {
    final exception = mapDioError(withResponse(401, {'message': 'Identifiants invalides.'}));
    expect(exception.statusCode, 401);
    expect(exception.message, 'Identifiants invalides.');
  });

  // Le message du serveur est repris pour un 409.
  test('mappe 409 avec le message du serveur', () {
    final exception = mapDioError(
      withResponse(409, {'message': 'Solde insuffisant pour effectuer ce transfert.'}),
    );
    expect(exception.statusCode, 409);
    expect(exception.message, contains('Solde insuffisant'));
  });

  // Les erreurs de validation par champ sont agrégées dans le message.
  test('mappe 400 avec les erreurs de validation par champ', () {
    final exception = mapDioError(withResponse(400, {
      'message': 'La validation des champs a échoué.',
      'fieldErrors': {'email': "L'email doit être valide."},
    }));
    expect(exception.statusCode, 400);
    expect(exception.message, contains('email doit être valide'));
  });

  // En l'absence de corps, un message générique est renvoyé.
  test('message générique si le corps est absent', () {
    final exception = mapDioError(withResponse(500, null));
    expect(exception.statusCode, 500);
    expect(exception.message, isNotEmpty);
  });
}
