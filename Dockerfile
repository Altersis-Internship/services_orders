FROM eclipse-temurin:17-jdk-jammy
COPY target/*.jar /app/monapp.jar
WORKDIR /app
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "monapp.jar"]
