import 'package:flutter_test/flutter_test.dart';
import 'package:minitransfer/core/utils/greeting.dart';

void main() {
  test('extrait le prénom du nom complet', () {
    expect(GreetingUtils.firstName('Alice Martin'), 'Alice');
    expect(GreetingUtils.firstName('Bob'), 'Bob');
    expect(GreetingUtils.firstName(''), 'Utilisateur');
  });
}
