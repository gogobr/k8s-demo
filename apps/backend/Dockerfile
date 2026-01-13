FROM eclipse-temurin:21-jdk-alpine

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

LABEL maintainer="mx"

COPY target/*.jar app.jar

ENTRYPOINT ["java","-Dfile.encoding=UTF-8", "-jar","app.jar"]