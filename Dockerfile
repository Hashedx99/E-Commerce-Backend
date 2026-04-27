# Single-stage image: build and run in the same container image
FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

# Copy sources needed for Maven build
COPY pom.xml /app/pom.xml
COPY src /app/src

# Build the Spring Boot jar during docker build
RUN mvn clean package -DskipTests

# Keep a stable runtime jar path
RUN mkdir -p /app/logs && cp /app/target/ecombend-0.0.1-SNAPSHOT.jar /app/app.jar

VOLUME /tmp
EXPOSE 8080

# Run the jar with prod profile by default
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","-jar","/app/app.jar"]
