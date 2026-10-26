# Dockerfile multi-stage para construir y ejecutar Spring Boot
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copiar wrapper y pom primero para aprovechar cache de dependencias
COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src src
RUN ./mvnw clean package -DskipTests

# Imagen final ligera para ejecución
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
