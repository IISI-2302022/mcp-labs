FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.22-1.1747241886

LABEL MAINTAINER=ming

WORKDIR /app/

COPY ./target/spring-ai-mcp-server.jar /app/
COPY --from=parent ./opentelemetry-javaagent.jar /app/

ENTRYPOINT ["bash", "-c" , "java -javaagent:/app/opentelemetry-javaagent.jar -jar /app/spring-ai-mcp-server.jar"]