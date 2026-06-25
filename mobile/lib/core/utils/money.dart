import 'package:intl/intl.dart';

/// Formate un montant entier (FCFA) avec séparateur de milliers, ex. `10,000 FCFA`.
String formatFcfa(int amount) {
  final formatter = NumberFormat('#,##0', 'en_US');
  return '${formatter.format(amount)} FCFA';
}
