import 'package:flutter/material.dart';

/// Thème visuel de l'application MiniTransfer (Material 3).
class AppTheme {
  const AppTheme._();

  /// Couleur principale (teal), reprise de l'identité visuelle du sujet.
  static const Color _seed = Color(0xFF00796B);

  static ThemeData light() {
    final colorScheme = ColorScheme.fromSeed(seedColor: _seed);
    return ThemeData(
      useMaterial3: true,
      colorScheme: colorScheme,
      appBarTheme: AppBarTheme(
        backgroundColor: colorScheme.primary,
        foregroundColor: colorScheme.onPrimary,
        centerTitle: true,
      ),
    );
  }
}
