FROM gradle:9.1-jdk21 AS development

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
COPY src ./src

EXPOSE 8080

CMD ["./gradlew", "bootRun"]

FROM development AS build

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre AS production

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
