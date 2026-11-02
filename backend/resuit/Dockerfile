FROM gradle:9.1-jdk21

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x ./gradlew
COPY src ./src

EXPOSE 8080

CMD ["./gradlew", "bootRun"]
