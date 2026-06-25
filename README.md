# MiniTransfer

**Test technique - Développeur Fullstack Junior**  
Réf. : `TGB-TT-STG-FSJ-2026-001` - TGB Solutions SARL

MiniTransfer est une plateforme simplifiée de transfert d'argent : un utilisateur s'inscrit, reçoit un portefeuille crédité de **10 000 FCFA**, consulte son solde, transfère de l'argent à un autre utilisateur (par email ou téléphone) et consulte l'historique de ses transactions.

**Stack :** Flutter (Android) · Java / Spring Boot 3 · MongoDB 7 · Docker

---

## Table des matières

1. Choix techniques
2. Structure du dépôt
3. Prérequis
4. Installation et lancement
5. Application mobile
6. Endpoints de l'API REST
7. Tests
8. Bonus livrés
9. Limites connues
10. Temps passé

---

## Choix techniques

### Backend - Spring Boot 3.5 / Java 21

- **Spring Boot 3.5.15** avec **Java 21** (Temurin), build **Maven** (`./mvnw`).
- **Spring Security** + **JWT** (jjwt 0.12) : authentification stateless, mot de passe hashé en **BCrypt**.
- **Spring Data MongoDB** pour la persistance.
- **Validation** (`jakarta.validation`) sur les DTOs d'entrée.
- **Gestion centralisée des erreurs** (`@RestControllerAdvice`) : corps JSON uniforme (`timestamp`, `status`, `error`, `message`, `path`, `fieldErrors`).
- **Transactions multi-documents** (`@Transactional` + `MongoTransactionManager`) : débit émetteur, crédit destinataire et enregistrement de la transaction réussissent ou échouent ensemble - on ne perd ni ne crée jamais d'argent.
- **Pas de Lombok** : DTOs en `record`, documents en classes simples (lisibilité pour l'évaluateur).

### MongoDB - modélisation

Deux collections, conformément au sujet :

| Collection       | Contenu |
|------------------|---------|
| `users`          | Utilisateur + **solde embarqué** (`balance` en `long`, FCFA) |
| `transactions`   | Émetteur, destinataire, montant, date, statut + noms dénormalisés |

**Pourquoi le solde dans `users` ?**

- Le sujet suggère « users (utilisateurs + solde) ».
- Relation 1:1 utilisateur ↔ portefeuille : moins de documents à mettre à jour par transfert.
- Débit/crédit via `$inc` atomique + transaction multi-documents pour la cohérence globale.

**Champs principaux :**

- `users` : `name`, `email` (index unique), `phone` (index unique), `passwordHash`, `balance`, `createdAt`.
- `transactions` : `senderId`, `recipientId`, `senderName`, `recipientName`, `amount`, `timestamp`, `status` (`COMPLETED`).

**Argent en entier :** tous les montants sont des `long` (FCFA), jamais de flottants.

**Replica set `rs0` :** MongoDB tourne en replica set mono-nœud (Docker). C'est une exigence technique pour activer les **transactions ACID** multi-documents - sans replica set, Spring ne pourrait pas garantir l'atomicité des transferts.

> **Note :** le conteneur MongoDB écoute sur le port **27018** (et non 27017) pour éviter les conflits avec une instance MongoDB locale déjà installée sous Windows.

### Mobile - Flutter / Riverpod

- **Flutter 3.44** / **Dart 3.12**.
- **Riverpod** (`flutter_riverpod`) pour la gestion d'état :
  - `AsyncValue` modèle uniformément *chargement / erreur / donnée* - adapté aux indicateurs de chargement et aux messages d'erreur conviviaux demandés par le sujet.
  - Providers testables et surchargeables (`mocktail` en tests), sans `BuildContext`.
  - Découplage clair entre logique métier et widgets.
- **Dio** : client HTTP avec intercepteur JWT et mapping centralisé des erreurs API.
- **go_router** : navigation déclarative + garde d'authentification.
- **flutter_secure_storage** : persistance sécurisée du token JWT et du profil utilisateur (Keystore Android).
- **intl** : formatage des montants FCFA (`10,000 FCFA`).

**Écrans livrés :** inscription, connexion, tableau de bord (solde, stats, transactions récentes), transfert, historique filtrable (Tous / Émis / Reçus), menu latéral profil + déconnexion, navigation par onglets.

---

## Structure du dépôt

```
MiniTransfer/
├── README.md
├── docker-compose.yml          # MongoDB + backend (une commande)
├── api/
│   └── MiniTransfer.postman_collection.json
├── docs/                       # captures / vidéo de démo (DL2)
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/tgbsolutions/minitransfer/
│       ├── auth/    wallet/    transfer/    user/
│       ├── security/    common/    config/
│       └── ...
└── mobile/
    ├── pubspec.yaml
    └── lib/
        ├── core/               # API, router, thème, stockage
        └── features/
            ├── auth/   wallet/   transfer/   history/
```

---

## Prérequis

| Outil | Version testée |
|-------|----------------|
| **JDK** | 21 (Temurin recommandé) |
| **Maven** | via wrapper `./mvnw` (inclus) |
| **MongoDB** | 7 (fourni par Docker) |
| **Docker Desktop** | récent (Compose V2) |
| **Flutter** | 3.44.1 (stable) |
| **Dart** | 3.12.1 |
| **Android SDK** | pour émulateur ou build APK |

Vérifications :

```bash
java -version          # → 21
docker compose version
flutter doctor
```

---

## Installation et lancement

### Option A - Tout en une commande (recommandé)

À la racine du dépôt `MiniTransfer/` :

```bash
docker compose up --build
```

Services démarrés :

| Service | URL / port |
|---------|------------|
| API backend | http://localhost:8080 |
| MongoDB | `localhost:27018` (replica set `rs0`) |
| Santé | http://localhost:8080/actuator/health |
| Swagger UI | http://localhost:8080/swagger-ui.html |

Attendre que `minitransfer-mongo` soit **healthy** et que le backend réponde `{"status":"UP"}` sur `/actuator/health`.

Variables d'environnement optionnelles (déjà définies dans `docker-compose.yml`) :

| Variable | Description | Défaut |
|----------|-------------|--------|
| `MONGODB_URI` | URI MongoDB | `mongodb://mongodb:27018/minitransfer?replicaSet=rs0` |
| `JWT_SECRET` | Clé HMAC JWT | valeur de dev (à changer en production) |
| `JWT_EXPIRATION_MS` | Durée du token (ms) | `86400000` (24 h) |

### Option B - Backend en local (hors Docker)

1. Démarrer MongoDB seul :

   ```bash
   docker compose up -d mongodb
   ```

2. Lancer l'API :

   ```bash
   cd backend
   ./mvnw spring-boot:run        # Linux / macOS
   .\mvnw.cmd spring-boot:run    # Windows
   ```

   L'URI par défaut dans `application.yml` pointe vers `mongodb://localhost:27018/minitransfer?replicaSet=rs0`.

### Arrêter les services

```bash
docker compose down          # conserve les données (volume mongo_data)
docker compose down -v       # supprime aussi le volume MongoDB
```

---

## Application mobile

### URL de l'API

| Contexte | URL de base |
|----------|-------------|
| **Émulateur Android** (défaut) | `http://10.0.2.2:8080` |
| **Appareil physique** (même réseau Wi-Fi) | `http://<IP-LAN-de-votre-PC>:8080` |

La constante est définie dans `mobile/lib/core/config/app_config.dart` et surchargeable au build :

```bash
flutter run --dart-define=API_BASE_URL=http://192.168.1.42:8080
```

### Lancer en développement

```bash
cd mobile
flutter pub get
flutter run                              # émulateur ou appareil connecté
```

### Build APK (partage / installation)

**APK de release** (optimisé, signé en debug par défaut - suffisant pour une démo) :

```bash
cd mobile
flutter pub get
flutter build apk --release
```

Fichier produit :

```
mobile/build/app/outputs/flutter-apk/app-release.apk
```

**APK de debug** (build plus rapide, pour tests internes) :

```bash
flutter build apk --debug
# → mobile/build/app/outputs/flutter-apk/app-debug.apk
```

**APK par architecture** (taille réduite) :

```bash
flutter build apk --split-per-abi --release
# → app-armeabi-v7a-release.apk, app-arm64-v8a-release.apk, app-x86_64-release.apk
```

> Sur un **appareil physique**, le backend doit être joignable depuis le téléphone : même réseau, pare-feu Windows autorisant le port 8080, et `API_BASE_URL` pointant vers l'IP locale de la machine hôte (pas `localhost`).

### Parcours de test manuel (mobile)

1. `docker compose up --build` → backend UP.
2. `cd mobile && flutter run` sur émulateur.
3. **Inscription** → tableau de bord avec **10 000 FCFA**.
4. Inscrire un second utilisateur (autre email) via Swagger ou Postman.
5. **Transférer** un montant → solde mis à jour, historique visible.
6. Tester les erreurs : solde insuffisant (409), destinataire inconnu (404), auto-transfert (400).
7. **Déconnexion** (menu latéral) → reconnexion avec un autre compte → données du bon utilisateur.

---

## Endpoints de l'API REST

Toutes les routes protégées exigent l'en-tête :

```
Authorization: Bearer <token>
```

### 1. Inscription

`POST /api/auth/register` → **201 Created**

**Corps :**

```json
{
  "name": "Alice Demo",
  "email": "alice@example.com",
  "phone": "+237600000001",
  "password": "password123"
}
```

**Réponse (exemple) :**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": "674a1b2c3d4e5f6789012345",
    "name": "Alice Demo",
    "email": "alice@example.com",
    "phone": "+237600000001",
    "balance": 10000
  }
}
```

**Erreurs courantes :** `400` (validation), `409` (email ou téléphone déjà utilisé).

---

### 2. Connexion

`POST /api/auth/login` → **200 OK**

**Corps :**

```json
{
  "email": "alice@example.com",
  "password": "password123"
}
```

**Réponse :** même structure que l'inscription (`token` + `user`).

**Erreurs :** `401` (identifiants invalides).

---

### 3. Solde du portefeuille

`GET /api/wallet/balance` → **200 OK** (authentifié)

**Réponse :**

```json
{
  "balance": 10000,
  "currency": "FCFA"
}
```

---

### 4. Effectuer un transfert

`POST /api/transfers` → **201 Created** (authentifié)

**Corps :**

```json
{
  "recipient": "bob@example.com",
  "amount": 3000
}
```

`recipient` accepte un **email** ou un **numéro de téléphone**.

**Réponse (exemple) :**

```json
{
  "transactionId": "674a1b2c3d4e5f6789012346",
  "recipientName": "Bob Demo",
  "amount": 3000,
  "timestamp": "2026-06-25T14:30:00.123Z",
  "status": "COMPLETED",
  "newBalance": 7000
}
```

**Erreurs :**

| Code | Cas |
|------|-----|
| `400` | Montant ≤ 0, auto-transfert, validation |
| `401` | Token absent ou invalide |
| `404` | Destinataire introuvable |
| `409` | Solde insuffisant |

---

### 5. Historique des transactions

`GET /api/transfers/history` → **200 OK** (authentifié)

Transactions **émises et reçues**, triées par date **décroissante**.

**Réponse (exemple) :**

```json
[
  {
    "transactionId": "674a1b2c3d4e5f6789012346",
    "direction": "SENT",
    "counterpartyName": "Bob Demo",
    "amount": 3000,
    "timestamp": "2026-06-25T14:30:00.123Z",
    "status": "COMPLETED"
  },
  {
    "transactionId": "674a1b2c3d4e5f6789012347",
    "direction": "RECEIVED",
    "counterpartyName": "Carol Demo",
    "amount": 1500,
    "timestamp": "2026-06-25T12:00:00.000Z",
    "status": "COMPLETED"
  }
]
```

`direction` : `SENT` (émise) ou `RECEIVED` (reçue), du point de vue de l'utilisateur connecté.

---

### Format d'erreur standard

```json
{
  "timestamp": "2026-06-25T14:30:00.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Solde insuffisant pour effectuer ce transfert.",
  "path": "/api/transfers",
  "fieldErrors": null
}
```

Pour une erreur de validation (`400`), `fieldErrors` contient les messages par champ.

---

## Tests

### Backend (58 tests)

```bash
cd backend
./mvnw test          # Linux / macOS
.\mvnw.cmd test      # Windows
```

Couverture : repositories (Testcontainers MongoDB), services métier (transferts, conservation de l'argent, rollback), contrôleurs (MockMvc), JWT, gestion d'erreurs, OpenAPI.

**Dernière exécution :** 58 tests, 0 échec.

### Mobile (35 tests)

```bash
cd mobile
flutter analyze      # aucun avertissement bloquant
flutter test
```

Couverture : contrôleurs Riverpod, repositories, écrans (widget tests), stockage sécurisé, formatage FCFA.

**Dernière exécution :** 35 tests, 0 échec.

### Vérification de bout en bout

1. `docker compose up --build` → MongoDB healthy + backend `UP`.
2. Postman : importer `api/MiniTransfer.postman_collection.json`, exécuter le flux complet (inscription Alice & Bob → solde → transfert → historique).
3. Émulateur : `flutter run` → même parcours via l'interface.
4. `./mvnw test` + `flutter test` → tout vert.

---

## Bonus livrés

| Bonus | Détail |
|-------|--------|
| **Docker** | `Dockerfile` multi-étapes (JDK 21 build → JRE 21 runtime) + `docker-compose.yml` |
| **Swagger / OpenAPI** | http://localhost:8080/swagger-ui.html |
| **Collection Postman** | `api/MiniTransfer.postman_collection.json` (capture automatique du token JWT) |
| **Tests** | 58 tests backend + 35 tests mobile |
| **Vidéo de démo** | à déposer dans `docs/` (étape DL2) |

---

## Limites connues

| Limite | Détail |
|--------|--------|
| **iOS** | Non développé (environnement Windows ; Android prioritaire conformément au sujet). |
| **Profil utilisateur mobile** | Nom et email sont persistés localement à l'inscription/connexion. Pas d'endpoint `GET /api/auth/me` : une session très ancienne (avant cette persistance) affiche un accueil générique jusqu'à la prochaine connexion. |
| **Port MongoDB** | `27018` au lieu de `27017` pour éviter les conflits locaux - documenté dans `docker-compose.yml` et `application.yml`. |
| **JWT de développement** | Secret par défaut dans `docker-compose.yml` ; à remplacer en production. |
| **Signature APK release** | Build release Flutter signé avec la clé debug par défaut ; pour un déploiement Play Store, configurer un keystore de release. |
| **Vidéo** | Non incluse dans ce commit (prévue en DL2). |

---

## Temps passé

**Environ 6 jours calendaires** (durée recommandée par le sujet : 5 à 7 jours), répartis ainsi :

- Backend (API, sécurité, transferts transactionnels, tests) : ~3 jours
- Mobile Flutter (écrans, intégration API, UX, tests) : ~2 jours
- Docker, documentation, validation de bout en bout : ~1 jour

---

## Auteur

Projet réalisé dans le cadre du test technique TGB Solutions SARL - réf. **TGB-TT-STG-FSJ-2026-001**.
