import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/utils/money.dart';
import '../../../core/utils/validators.dart';
import '../../wallet/application/wallet_controller.dart';
import '../data/transfer_repository.dart';

/// Écran de transfert : destinataire (email ou téléphone) + montant.
class TransferScreen extends ConsumerStatefulWidget {
  const TransferScreen({super.key});

  @override
  ConsumerState<TransferScreen> createState() => _TransferScreenState();
}

class _TransferScreenState extends ConsumerState<TransferScreen> {
  final _formKey = GlobalKey<FormState>();
  final _recipientController = TextEditingController();
  final _amountController = TextEditingController();
  bool _submitting = false;

  @override
  void dispose() {
    _recipientController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  String? _validateAmount(String? value) {
    if (value == null || value.trim().isEmpty) return 'Le montant est obligatoire.';
    final amount = int.tryParse(value.trim());
    if (amount == null) return 'Montant invalide.';
    if (amount <= 0) return 'Le montant doit être strictement positif.';
    return null;
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    // Capturé avant l'await pour un usage sûr après l'opération asynchrone.
    final messenger = ScaffoldMessenger.of(context);
    setState(() => _submitting = true);
    try {
      final result = await ref.read(transferRepositoryProvider).transfer(
            recipient: _recipientController.text.trim(),
            amount: int.parse(_amountController.text.trim()),
          );
      // Rafraîchit le solde affiché sur l'accueil avant d'y revenir.
      await ref.read(walletControllerProvider.notifier).refresh();
      if (!mounted) return;
      messenger.showSnackBar(SnackBar(
        content: Text(
          'Transfert de ${formatFcfa(result.amount)} à ${result.recipientName} effectué.',
        ),
      ));
      context.pop();
    } on ApiException catch (error) {
      messenger.showSnackBar(SnackBar(content: Text(error.message)));
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Transfert')),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Form(
            key: _formKey,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: 8),
                Text('Envoyer de l\'argent', style: Theme.of(context).textTheme.headlineSmall),
                const SizedBox(height: 24),
                TextFormField(
                  controller: _recipientController,
                  textInputAction: TextInputAction.next,
                  decoration: const InputDecoration(
                    labelText: 'Destinataire (email ou téléphone)',
                    prefixIcon: Icon(Icons.person_outline),
                  ),
                  validator: (value) =>
                      Validators.notEmpty(value, 'Le destinataire est obligatoire.'),
                ),
                const SizedBox(height: 16),
                TextFormField(
                  controller: _amountController,
                  keyboardType: TextInputType.number,
                  inputFormatters: [FilteringTextInputFormatter.digitsOnly],
                  textInputAction: TextInputAction.done,
                  decoration: const InputDecoration(
                    labelText: 'Montant (FCFA)',
                    prefixIcon: Icon(Icons.payments_outlined),
                  ),
                  validator: _validateAmount,
                  onFieldSubmitted: (_) => _submit(),
                ),
                const SizedBox(height: 24),
                FilledButton(
                  onPressed: _submitting ? null : _submit,
                  child: _submitting
                      ? const SizedBox(
                          height: 20,
                          width: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : const Text('Envoyer'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
