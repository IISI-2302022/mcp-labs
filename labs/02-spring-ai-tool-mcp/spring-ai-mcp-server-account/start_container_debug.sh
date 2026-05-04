#!/bin/bash

# 基本環境變數
export THIS_SHELL_PATH="$(readlink -f "$0")"
export THIS_SHELL_DIR="$(dirname "${THIS_SHELL_PATH}")"

export BUILD_JAR_SHELL="${THIS_SHELL_DIR}/../build.sh"
chmod u+x "${BUILD_JAR_SHELL}"
"${BUILD_JAR_SHELL}" || exit 1

if type podman &> /dev/null; then
  export container_engine=podman
elif type docker &> /dev/null; then
  export container_engine=docker
else
  echo "Error: No container engine found. Please install Podman or Docker." >&2
  exit 1
fi

export app_name=spring-ai-mcp-server-account

export MY_JAVA_APP_PORT=8989
export MY_JAVA_DEBUG_PORT=56216

"${container_engine}" build \
  -t "${app_name}:latest" \
  -f "${THIS_SHELL_DIR}/Dockerfile" || exit 1

podman images | grep "${app_name}"

export MY_JAVA_DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:${MY_JAVA_DEBUG_PORT}"

"${container_engine}" rm -f "${app_name}"

"${container_engine}" run \
  -d \
  --rm \
  --name "${app_name}" \
  -p "${MY_JAVA_APP_PORT}:${MY_JAVA_APP_PORT}" \
  -p "${MY_JAVA_DEBUG_PORT}:${MY_JAVA_DEBUG_PORT}" \
  -e "JAVA_TOOL_OPTIONS=${MY_JAVA_DEBUG_OPTS}" \
  "${app_name}:latest"
