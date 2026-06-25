import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_error_mapper.dart';
import '../../../core/network/dio_provider.dart';
import '../domain/transfer_result.dart';

/// Accès distant aux transferts (exécution d'un transfert).
class TransferRepository {
  TransferRepository(this._dio);

  final Dio _dio;

  Future<TransferResult> transfer({required String recipient, required int amount}) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/api/transfers',
        data: {'recipient': recipient, 'amount': amount},
      );
      return TransferResult.fromJson(response.data!);
    } on DioException catch (error) {
      throw mapDioError(error);
    }
  }
}

final transferRepositoryProvider = Provider<TransferRepository>(
  (ref) => TransferRepository(ref.read(dioProvider)),
);
