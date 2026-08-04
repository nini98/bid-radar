FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x gradlew && ./gradlew bootJar -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ENV TZ=UTC
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xmx400m", "-Xms128m", "-jar", "app.jar"]
