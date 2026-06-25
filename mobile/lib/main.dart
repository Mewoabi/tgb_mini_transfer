import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';

void main() {
  // ProviderScope est la racine de la gestion d'état Riverpod : il rend les providers
  // disponibles dans tout l'arbre de widgets.
  runApp(const ProviderScope(child: MiniTransferApp()));
}
