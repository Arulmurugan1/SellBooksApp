FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /build
COPY pom.xml .
COPY api-gateway ./api-gateway
COPY common-library ./common-library
RUN mkdir -p /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0 && \
    cp pom.xml /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0/book-selling-app-1.0.0.pom && \
    mvn -f api-gateway/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/api-gateway/target/api-gateway-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
