import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/dio_error_mapper.dart';
import '../../../core/network/dio_provider.dart';
import '../domain/auth_result.dart';

/// Accès distant à l'API d'authentification.
///
/// En cas d'erreur HTTP/réseau, lève une [ApiException] au message convivial
/// (traduite depuis [DioException]).
class AuthRepository {
  AuthRepository(this._dio);

  final Dio _dio;

  Future<AuthResult> register({
    required String name,
    required String email,
    required String phone,
    required String password,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/api/auth/register',
        data: {'name': name, 'email': email, 'phone': phone, 'password': password},
      );
      return AuthResult.fromJson(response.data!);
    } on DioException catch (error) {
      throw mapDioError(error);
    }
  }

  Future<AuthResult> login({required String email, required String password}) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/api/auth/login',
        data: {'email': email, 'password': password},
      );
      return AuthResult.fromJson(response.data!);
    } on DioException catch (error) {
      throw mapDioError(error);
    }
  }
}

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepository(ref.read(dioProvider)),
);
