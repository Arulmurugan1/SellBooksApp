FROM maven:3.9-eclipse-temurin-17
WORKDIR /build

COPY pom.xml .
COPY common-library ./common-library
COPY eureka-server ./eureka-server

RUN mkdir -p /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0 && \
    cp pom.xml /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0/book-selling-app-1.0.0.pom && \
    mvn -f eureka-server/pom.xml dependency:resolve -DskipTests -q

EXPOSE 8761

CMD ["mvn", "-f", "eureka-server/pom.xml", "spring-boot:run"]
