# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw dependency:resolve

COPY . .

RUN chmod +x ./mvnw
CMD ["./mvnw", "spring-boot:run"]