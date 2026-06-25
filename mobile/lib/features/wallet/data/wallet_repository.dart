import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_error_mapper.dart';
import '../../../core/network/dio_provider.dart';
import '../domain/wallet_balance.dart';

/// Accès distant au portefeuille (consultation du solde).
class WalletRepository {
  WalletRepository(this._dio);

  final Dio _dio;

  Future<WalletBalance> getBalance() async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/api/wallet/balance');
      return WalletBalance.fromJson(response.data!);
    } on DioException catch (error) {
      throw mapDioError(error);
    }
  }
}

final walletRepositoryProvider = Provider<WalletRepository>(
  (ref) => WalletRepository(ref.read(dioProvider)),
);
