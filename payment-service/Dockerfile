FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /build
COPY pom.xml .
COPY payment-service ./payment-service
COPY common-library ./common-library
RUN mvn -N install -DskipTests && mvn -f common-library/pom.xml clean install -DskipTests && mvn -f payment-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/payment-service/target/payment-service-1.0.0.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
