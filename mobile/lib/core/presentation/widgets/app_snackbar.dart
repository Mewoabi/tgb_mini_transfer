import 'package:flutter/material.dart';

/// Affiche un message via une SnackBar unifiée.
class AppSnackBar {
  const AppSnackBar._();

  static void show(BuildContext context, String message) {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(SnackBar(content: Text(message)));
  }
}
