# Dockerfile
FROM eclipse-temurin:17-jdk-jammy

# Copier le JAR directement depuis target/
COPY target/services_orders-0.0.1-SNAPSHOT.jar /app/monapp.jar

# Définir le dossier de travail
WORKDIR /app

# Exposer le port de l'application
EXPOSE 8082

# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "monapp.jar"]
