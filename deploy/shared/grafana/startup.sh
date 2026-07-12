#!/usr/bin/env sh
set -eu

grafana_database="/var/lib/grafana/grafana.db"

if [ -f "$grafana_database" ]; then
  echo "Synchronizing Grafana admin password from ADMIN_MASTER_PASSWORD"
  grafana cli --homepath /usr/share/grafana admin reset-admin-password "$GF_SECURITY_ADMIN_PASSWORD"
else
  echo "Bootstrapping Grafana admin password from GF_SECURITY_ADMIN_PASSWORD"
fi

exec /run.sh
