import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/utils/money.dart';

void main() {
  // Le montant est groupé par milliers et suffixé « FCFA ».
  test('formate un montant avec séparateur de milliers', () {
    final formatted = formatFcfa(10000);
    expect(formatted, contains('FCFA'));
    expect(formatted, contains('10'));
    expect(formatted, contains('000'));
  });

  test('formate zéro', () {
    expect(formatFcfa(0), contains('FCFA'));
  });
}
