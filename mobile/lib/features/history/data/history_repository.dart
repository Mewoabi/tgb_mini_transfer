import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_error_mapper.dart';
import '../../../core/network/dio_provider.dart';
import '../domain/transaction_item.dart';

/// Accès distant à l'historique des transactions.
class HistoryRepository {
  HistoryRepository(this._dio);

  final Dio _dio;

  Future<List<TransactionItem>> getHistory() async {
    try {
      final response = await _dio.get<List<dynamic>>('/api/transfers/history');
      final data = response.data ?? const [];
      return data
          .map((item) => TransactionItem.fromJson(item as Map<String, dynamic>))
          .toList();
    } on DioException catch (error) {
      throw mapDioError(error);
    }
  }
}

final historyRepositoryProvider = Provider<HistoryRepository>(
  (ref) => HistoryRepository(ref.read(dioProvider)),
);
