FROM gradle:8.7-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon


FROM openjdk:17-alpine
EXPOSE 40019:40019
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/kupidon-kotlin-ktor.jar
ENTRYPOINT ["java", "-jar", "/app/kupidon-kotlin-ktor.jar"]


LABEL authors="rrain"
