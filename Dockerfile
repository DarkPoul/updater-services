FROM openjdk:17-jdk-slim

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw dependency:resolve

COPY . .

RUN chmod +x ./mvnw
CMD ["./mvnw", "spring-boot:run"]