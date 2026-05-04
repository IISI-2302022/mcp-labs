#!/bin/bash

# 基本環境變數
export THIS_SHELL_PATH="$(readlink -f "$0")"
export THIS_SHELL_DIR="$(dirname "${THIS_SHELL_PATH}")"

#export BUILD_JAR_SHELL="${THIS_SHELL_DIR}/../build.sh"
#chmod u+x "${BUILD_JAR_SHELL}"
#"${BUILD_JAR_SHELL}" || exit 1

#export JAVA_HOME="自己填"
#export PATH="${JAVA_HOME}/bin:${PATH}"

export MY_JAVA_DEBUG_PORT=56216
export MY_JAVA_DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${MY_JAVA_DEBUG_PORT}"
java ${MY_JAVA_DEBUG_OPTS} -jar "${THIS_SHELL_DIR}/target/spring-ai-mcp-server-account.jar"

