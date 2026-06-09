FROM maven:3.9-eclipse-temurin-17
WORKDIR /build

COPY pom.xml .
COPY common-library ./common-library
COPY payment-service ./payment-service

RUN mkdir -p /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0 && \
    cp pom.xml /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0/book-selling-app-1.0.0.pom && \
    mvn -f common-library/pom.xml clean install -DskipTests -q && \
    mvn -f payment-service/pom.xml dependency:resolve -DskipTests -q

EXPOSE 8084

CMD ["mvn", "-pl", "payment-service", "-am", "clean", "spring-boot:run"]
