#!/usr/bin/env sh
set -eu

embedded_server_host="${EMBEDDED_SERVER_HOST:?EMBEDDED_SERVER_HOST is required}"
escaped_embedded_server_host=$(printf '%s' "$embedded_server_host" | sed 's/[&|]/\\&/g')

sed "s|__EMBEDDED_SERVER_HOST__|$escaped_embedded_server_host|g" \
  /etc/prometheus/prometheus.yml \
  > /tmp/prometheus.yml

exec /bin/prometheus --config.file=/tmp/prometheus.yml "$@"
