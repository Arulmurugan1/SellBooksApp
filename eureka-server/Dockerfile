FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /build
COPY pom.xml .
COPY eureka-server ./eureka-server
COPY common-library ./common-library
RUN mkdir -p /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0 && \
    cp pom.xml /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0/book-selling-app-1.0.0.pom && \
    mvn -f eureka-server/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/eureka-server/target/eureka-server-1.0.0.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
