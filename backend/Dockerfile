# ============================================================
# Image du backend MiniTransfer (build multi-étapes)
# ============================================================

# --- Étape 1 : compilation et packaging avec Maven (JDK 21) ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src/ src/
# Construction du jar (les tests s'exécutent hors image, via `mvnw test`).
RUN mvn -B clean package -DskipTests

# --- Étape 2 : image d'exécution légère (JRE 21 seulement) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
