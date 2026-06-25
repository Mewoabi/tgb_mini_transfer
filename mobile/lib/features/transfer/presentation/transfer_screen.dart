import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/presentation/widgets/app_snackbar.dart';
import '../../../core/utils/money.dart';
import '../../../core/utils/validators.dart';
import '../../history/application/history_controller.dart';
import '../../wallet/application/wallet_controller.dart';
import '../data/transfer_repository.dart';

/// Formulaire de transfert (corps d'onglet) : destinataire + montant.
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

  void _clearForm() {
    _recipientController.clear();
    _amountController.clear();
    _formKey.currentState?.reset();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _submitting = true);
    try {
      final result = await ref.read(transferRepositoryProvider).transfer(
            recipient: _recipientController.text.trim(),
            amount: int.parse(_amountController.text.trim()),
          );
      await Future.wait([
        ref.read(walletControllerProvider.notifier).refresh(),
        ref.read(historyControllerProvider.notifier).refresh(),
      ]);
      if (!mounted) return;
      _clearForm();
      AppSnackBar.show(
        context,
        'Transfert de ${formatFcfa(result.amount)} à ${result.recipientName} effectué.',
      );
      StatefulNavigationShell.of(context).goBranch(0);
    } on ApiException catch (error) {
      if (mounted) AppSnackBar.show(context, error.message);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return SafeArea(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'Envoyer de l\'argent',
                style: theme.textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 8),
              Text(
                'Saisissez l\'email ou le téléphone du destinataire et le montant en FCFA.',
                style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.outline),
              ),
              const SizedBox(height: 24),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    children: [
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
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
              FilledButton.icon(
                onPressed: _submitting ? null : _submit,
                icon: _submitting
                    ? const SizedBox(
                        height: 20,
                        width: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.send),
                label: Text(_submitting ? 'Envoi en cours…' : 'Envoyer'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
