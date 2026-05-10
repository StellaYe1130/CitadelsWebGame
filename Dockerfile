FROM gradle:8.8-jdk17-alpine AS build
WORKDIR /app

COPY build.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/citadels.jar ./citadels.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "citadels.jar"]
