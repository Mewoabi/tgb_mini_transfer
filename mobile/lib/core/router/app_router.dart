import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/auth/application/auth_controller.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/wallet/presentation/home_screen.dart';
import '../presentation/splash_screen.dart';

/// Chemins de l'application.
class AppRoutes {
  const AppRoutes._();
  static const String splash = '/splash';
  static const String login = '/login';
  static const String home = '/home';
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
      final atLogin = location == AppRoutes.login;

      // Non connecté : tout renvoie vers la connexion.
      if (!loggedIn) {
        return atLogin ? null : AppRoutes.login;
      }

      // Connecté : on éloigne du splash et de la connexion.
      if (location == AppRoutes.splash || atLogin) {
        return AppRoutes.home;
      }
      return null;
    },
    routes: [
      GoRoute(path: AppRoutes.splash, builder: (_, _) => const SplashScreen()),
      GoRoute(path: AppRoutes.login, builder: (_, _) => const LoginScreen()),
      GoRoute(path: AppRoutes.home, builder: (_, _) => const HomeScreen()),
    ],
  );
});
