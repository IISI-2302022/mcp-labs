#!/bin/bash

# 基本環境變數
export THIS_SHELL_PATH="$(readlink -f "$0")"
export THIS_SHELL_DIR="$(dirname "${THIS_SHELL_PATH}")"

mvn -f "${THIS_SHELL_DIR}/../pom.xml" -am -pl "02-spring-ai-tool-mcp/spring-ai-mcp-client,02-spring-ai-tool-mcp/spring-ai-mcp-server,02-spring-ai-tool-mcp/spring-ai-mcp-server-account" clean package -DskipTests