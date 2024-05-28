# Використовуйте офіційний образ OpenJDK 17 як базовий cd ../
FROM eclipse-temurin:17-jdk-jammy

# Встановіть робочу директорію в контейнері
WORKDIR /app

# Скопіюйте файли Maven Wrapper і POM файл у контейнер
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Дайте виконувані права Maven Wrapper скрипту
RUN chmod +x ./mvnw

# Встановіть залежності
RUN ./mvnw dependency:resolve

# Скопіюйте всі інші файли в робочу директорію контейнера
COPY . .

# Дайте виконувані права Maven Wrapper скрипту знову (на випадок, якщо був переписаний)
RUN chmod +x ./mvnw

# Запустіть Spring Boot застосунок
CMD ["./mvnw", "spring-boot:run"]