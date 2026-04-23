#!/bin/bash

# 基本環境變數
export THIS_SHELL_PATH="$(readlink -f "$0")"
export THIS_SHELL_DIR="$(dirname "${THIS_SHELL_PATH}")"

mvn -f "${THIS_SHELL_DIR}/../pom.xml" -am -pl "03-spring-ai-mcp-auth-keycloak/spring-ai-mcp-client-security,03-spring-ai-mcp-auth-keycloak/spring-ai-mcp-server-account-security" clean package -DskipTests