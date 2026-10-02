#!/bin/sh
set -eu
java -Xmx256m -jar /app/app.jar --spring.profiles.active=local \
  --server.address=127.0.0.1 --server.port=8081 \
  --spring.datasource.url='jdbc:h2:file:/app/data/local/fracturecare;MODE=MySQL;DATABASE_TO_LOWER=TRUE' \
  --app.frontend-origin="${RENDER_EXTERNAL_URL:?Render must supply the external service URL}" &
backend_pid=$!
nginx -c /app/nginx.conf -g 'daemon off;' &
frontend_pid=$!
trap 'kill "$backend_pid" "$frontend_pid" 2>/dev/null || true' EXIT
trap 'exit 0' TERM INT
while kill -0 "$backend_pid" 2>/dev/null && kill -0 "$frontend_pid" 2>/dev/null; do
  sleep 2
done
exit 1
