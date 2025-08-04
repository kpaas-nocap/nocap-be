FROM eclipse-temurin:17 AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
RUN chmod +x gradlew
COPY src src
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]