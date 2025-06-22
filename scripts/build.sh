#!/bin/bash
echo "🛠️ Compilation du projet..."
mvn clean package -DskipTests

echo "🐳 Construction de l'image Docker..."
docker build -t monapp-orders-service:latest .
