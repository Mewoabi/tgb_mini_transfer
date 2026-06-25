import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/application/auth_controller.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/history/presentation/history_screen.dart';
import '../../features/transfer/presentation/transfer_screen.dart';
import '../../features/wallet/presentation/home_screen.dart';
import '../presentation/splash_screen.dart';

/// Chemins de l'application.
class AppRoutes {
  const AppRoutes._();
  static const String splash = '/splash';
  static const String login = '/login';
  static const String register = '/register';
  static const String home = '/home';
  static const String transfer = '/transfer';
  static const String history = '/history';
}

/// Routeur de l'application avec garde d'authentification.
///
/// Un [ValueNotifier] fait le pont entre l'état Riverpod ([authControllerProvider]) et
/// `refreshListenable` de go_router : à chaque changement d'état, les redirections sont réévaluées.
final goRouterProvider = Provider<GoRouter>((ref) {
  final authListenable = ValueNotifier<AuthStatus>(AuthStatus.unknown);
  ref.listen<AuthStatus>(
    authControllerProvider,
    (_, next) => authListenable.value = next,
    fireImmediately: true,
  );
  ref.onDispose(authListenable.dispose);

  return GoRouter(
    initialLocation: AppRoutes.splash,
    refreshListenable: authListenable,
    redirect: (context, state) {
      final status = authListenable.value;
      final location = state.matchedLocation;

      // Tant que la session n'est pas restaurée : on reste sur le splash.
      if (status == AuthStatus.unknown) {
        return location == AppRoutes.splash ? null : AppRoutes.splash;
      }

      final loggedIn = status == AuthStatus.authenticated;
      final atAuthRoute =
          location == AppRoutes.login || location == AppRoutes.register;

      // Non connecté : tout renvoie vers la connexion (sauf les écrans d'authentification).
      if (!loggedIn) {
        return atAuthRoute ? null : AppRoutes.login;
      }

      // Connecté : on éloigne du splash et des écrans d'authentification.
      if (location == AppRoutes.splash || atAuthRoute) {
        return AppRoutes.home;
      }
      return null;
    },
    routes: [
      GoRoute(path: AppRoutes.splash, builder: (_, _) => const SplashScreen()),
      GoRoute(path: AppRoutes.login, builder: (_, _) => const LoginScreen()),
      GoRoute(path: AppRoutes.register, builder: (_, _) => const RegisterScreen()),
      GoRoute(path: AppRoutes.home, builder: (_, _) => const HomeScreen()),
      GoRoute(path: AppRoutes.transfer, builder: (_, _) => const TransferScreen()),
      GoRoute(path: AppRoutes.history, builder: (_, _) => const HistoryScreen()),
    ],
  );
});
