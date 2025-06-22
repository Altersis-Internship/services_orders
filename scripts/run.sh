#!/bin/bash

set -e  # Arrête le script en cas d'erreur

APP_NAME="services_orders"
JAR_NAME="services_orders-0.0.1-SNAPSHOT.jar"
JAR_PATH="target/$JAR_NAME"
IMAGE_NAME="monapp-orders-service"
DOCKERFILE="Dockerfile"
COMPOSE_FILE="docker-compose.yml"

echo "=== 🛠️ Compilation du projet Maven ==="
mvn clean package -DskipTests

if [ ! -f "$JAR_PATH" ]; then
  echo "❌ Erreur : le fichier $JAR_PATH est introuvable."
  exit 1
fi

echo "✅ Compilation terminée : $JAR_PATH"

echo "=== 🐳 Construction de l'image Docker ==="
docker build -t $IMAGE_NAME:latest -f $DOCKERFILE .

echo "✅ Image Docker construite : $IMAGE_NAME"

echo "=== 🚀 Démarrage des services avec Docker Compose ==="
docker-compose -f $COMPOSE_FILE up -d --build

echo "🎉 Tous les services sont lancés."
echo "➡️ Accès :"
echo "  - Service      : http://localhost:8082"
echo "  - Prometheus   : http://localhost:9090"
echo "  - Grafana      : http://localhost:3000"
