import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/network/api_exception.dart';
import 'package:minitransfer/features/transfer/data/transfer_repository.dart';
import 'package:mocktail/mocktail.dart';

class _MockDio extends Mock implements Dio {}

void main() {
  late _MockDio dio;
  late TransferRepository repository;

  setUp(() {
    dio = _MockDio();
    repository = TransferRepository(dio);
  });

  // Un transfert réussi renvoie le résultat décodé.
  test('transfert réussi renvoie le résultat', () async {
    when(() => dio.post<Map<String, dynamic>>('/api/transfers', data: any(named: 'data')))
        .thenAnswer((_) async => Response<Map<String, dynamic>>(
              requestOptions: RequestOptions(path: '/api/transfers'),
              statusCode: 201,
              data: {
                'transactionId': 't1',
                'recipientName': 'Bob',
                'amount': 3000,
                'timestamp': '2026-01-01T00:00:00Z',
                'status': 'COMPLETED',
                'newBalance': 7000,
              },
            ));

    final result = await repository.transfer(recipient: 'bob@example.com', amount: 3000);

    expect(result.recipientName, 'Bob');
    expect(result.amount, 3000);
    expect(result.newBalance, 7000);
  });

  // Une erreur HTTP (ex. solde insuffisant) est convertie en ApiException.
  test('un échec est converti en ApiException avec le bon code', () async {
    when(() => dio.post<Map<String, dynamic>>('/api/transfers', data: any(named: 'data')))
        .thenThrow(DioException(
      requestOptions: RequestOptions(path: '/api/transfers'),
      type: DioExceptionType.badResponse,
      response: Response(
        requestOptions: RequestOptions(path: '/api/transfers'),
        statusCode: 409,
        data: {'message': 'Solde insuffisant pour effectuer ce transfert.'},
      ),
    ));

    await expectLater(
      repository.transfer(recipient: 'bob@example.com', amount: 999999),
      throwsA(isA<ApiException>()
          .having((e) => e.statusCode, 'statusCode', 409)
          .having((e) => e.message, 'message', contains('Solde insuffisant'))),
    );
  });
}
