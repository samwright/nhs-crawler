#!/usr/bin/env bash
set -e

# REQUIRED_SERVICES=app1,app2,app3
IFS=',' read -ra paths <<< $LOCAL_SERVICES

for path in "${paths[@]}"
do
    curl -f http://localhost:8080${path} > /dev/null 2>&1
done
