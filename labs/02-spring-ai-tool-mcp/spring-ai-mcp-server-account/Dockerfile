FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.24-2.1777302154

LABEL MAINTAINER=ming

ARG PORT_EXPOSED=8080
EXPOSE ${PORT_EXPOSED}

WORKDIR /app/

COPY ./target/spring-ai-mcp-server-account.jar /app/

ENTRYPOINT ["bash", "-c" , "java -jar /app/spring-ai-mcp-server-account.jar"]