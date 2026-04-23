#!/bin/bash

podman run -d \
  --name keycloak \
  --restart always \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -p 8080:8080 \
  keycloak/keycloak:26.2 \
  start-dev