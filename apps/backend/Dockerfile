FROM localhost/base-java:21

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

LABEL maintainer="mx"

COPY target/k8s-demo-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-Dfile.encoding=UTF-8", "-jar","app.jar"]