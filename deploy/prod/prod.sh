#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_root="$(CDPATH= cd -- "$script_dir/../.." && pwd)"

command="${1:-up}"
if [ "$#" -gt 0 ]; then
    shift
fi

case "$command" in
    up)
        set -- up -d --force-recreate "$@"
        ;;
    logs)
        set -- logs -f "$@"
        ;;
    *)
        set -- "$command" "$@"
        ;;
esac

docker compose \
    --env-file "$repo_root/.env" \
    -f "$script_dir/docker-compose.yml" \
    "$@"
