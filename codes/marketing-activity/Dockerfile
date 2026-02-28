# 1. 基础镜像
FROM eclipse-temurin:21-jre-jammy

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

# 2. 设置工作目录
WORKDIR /app

# 3. 【关键】下载 OpenTelemetry Java Agent
# 生产环境通常会把这个 jar 包打在基础镜像里，或者挂载进去，这里演示直接下载
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.0.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

LABEL maintainer="mx"

# 4. 复制你的业务 Jar 包
COPY target/*.jar app.jar

# 5. 【关键】修改启动命令，挂载探针
# 注意：-javaagent 必须在 -jar 之前
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]