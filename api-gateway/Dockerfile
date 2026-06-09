FROM maven:3.9-eclipse-temurin-17
WORKDIR /build

# Cache dependencies first
COPY pom.xml .
COPY common-library ./common-library
COPY api-gateway ./api-gateway

# Install parent pom and common-library so module resolves
RUN mkdir -p /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0 && \
    cp pom.xml /root/.m2/repository/com/booksellingapp/book-selling-app/1.0.0/book-selling-app-1.0.0.pom && \
    mvn -f common-library/pom.xml clean install -DskipTests -q && \
    mvn -f api-gateway/pom.xml dependency:resolve -DskipTests -q

EXPOSE 8080

# Run from root reactor so upstream modules are rebuilt and latest source is used.
CMD ["sh", "-c", "mvn -f common-library/pom.xml clean install -DskipTests -q && mvn -f api-gateway/pom.xml spring-boot:run"]
