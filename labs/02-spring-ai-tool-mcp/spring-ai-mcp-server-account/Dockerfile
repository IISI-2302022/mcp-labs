FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24-2.1777302154

LABEL MAINTAINER=ming

WORKDIR /app/

ARG JAR_PATH=./target/spring-ai-mcp-server-account.jar
COPY "${JAR_PATH}" /app/

ENTRYPOINT ["/bin/sh", "-c", \
            "java \"${MY_JAVA_DEBUG_OPTS}\" -XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=75 -XX:+UseContainerSupport -Dfile.encoding=UTF-8 -jar \"${APP_NAME}\""]