# Budowanie
FROM openjdk:21-jdk-slim AS build

WORKDIR /app

# Kopiuje wszystkie wymagane pliki do Maven Wrapper
COPY .mvn/wrapper /app/.mvn/wrapper
COPY mvnw /app/mvnw
COPY pom.xml /app/pom.xml

# Nadanie uprawnień i pobranie zależności
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Wystawienie portu
EXPOSE 8090

# Skopiuj resztę projektu
COPY . .

# Buduj aplikację
RUN ./mvnw clean package -DskipTests

# Uruchamianie
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY --from=build /app/target/notifications-service-1.0.0-SNAPSHOT.jar ./notifications-service-1.0.0-SNAPSHOT.jar

CMD ["java", "-jar", "notifications-service-1.0.0-SNAPSHOT.jar"]