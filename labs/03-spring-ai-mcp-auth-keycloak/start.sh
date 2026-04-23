#!/bin/bash

# ============================================================
# 停用 MSYS(Git Bash) 自動路徑轉換
# ============================================================
# 在 Windows Git Bash (MSYS2/MinGW) 中，MSYS 會自動將「看起來像 POSIX 絕對路徑」的字串
# 轉換為 Windows 路徑，例如：
#   /d/project/kafka/data:/var/lib/kafka/data
#   → D:/project/kafka/data;C:/var/lib/kafka/data
#
# 冒號 ":" 被解讀為路徑分隔符，後面的 /var 被轉成 C:/var，
# 導致 docker -v 掛載的 volume 路徑變成 "...data;C/var/lib/kafka/data"，
# 進而在本機產生名為 ";C" 的異常目錄。
#
# 設定 MSYS_NO_PATHCONV=1 可完全關閉此自動轉換行為。
# （此變數僅對 MSYS/Git Bash 有效，在 Linux/macOS 上不會造成影響）
# ============================================================
export MSYS_NO_PATHCONV=1

export THIS_SHELL_PATH="$(readlink -f "$0")"
export THIS_SHELL_DIR="$(dirname "${THIS_SHELL_PATH}")"

if type podman &> /dev/null; then
  export container_engine=podman
elif type docker &> /dev/null; then
  export container_engine=docker
else
  echo "Error: No container engine found. Please install Podman or Docker." >&2
  exit 1
fi

"${container_engine}" network create --driver bridge mcp-labs

mkdir -p "${THIS_SHELL_DIR}/redis/data/"

# keycloak
"${container_engine}" run -d \
  --name keycloak \
  --network mcp-labs \
  --restart always \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -e KC_DB_USERNAME=admin \
  -e KC_DB_PASSWORD=admin \
  -e KC_DB=postgres \
  -e KC_DB_URL=jdbc:postgresql://postgresql:5432/keycloak \
  -v "${THIS_SHELL_DIR}/keycloak/data/:/opt/keycloak/data/import/" \
  -p 8080:8080 \
  keycloak/keycloak:26.2 \
  start-dev --import-realm

# postgres
"${container_engine}" run -d \
  --name postgresql \
  --network mcp-labs \
  --restart always \
  -p 5432:5432 \
  -e POSTGRES_DB=keycloak \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin \
  -v postgres-data-keycloak:/var/lib/postgresql/data \
  postgres:13.16

# redis
"${container_engine}" run -d \
  --name redis \
  --network mcp-labs \
  --restart always \
  -p 6379:6379 \
  -v "${THIS_SHELL_DIR}/redis/data/:/data/" \
  redis:8.6.2