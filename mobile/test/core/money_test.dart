import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/utils/money.dart';

void main() {
  // Le montant est groupé par milliers et suffixé « FCFA ».
  test('formate un montant avec séparateur de milliers', () {
    expect(formatFcfa(10000), '10,000 FCFA');
    expect(formatFcfa(9677), '9,677 FCFA');
  });

  test('formate zéro', () {
    expect(formatFcfa(0), '0 FCFA');
  });
}
