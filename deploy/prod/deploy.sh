#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_root="$(CDPATH= cd -- "$script_dir/../.." && pwd)"

command="${1:-up}"
if [ "$#" -gt 0 ]; then
    shift
fi

build_option_set=false
for arg in "$@"; do
    case "$arg" in
        --build|--no-build)
            build_option_set=true
            ;;
    esac
done

cd "$repo_root"
git pull origin main

chmod +x "$script_dir/prod.sh"

if [ "$command" = "up" ] && [ "$build_option_set" = false ]; then
    "$script_dir/prod.sh" "$command" --build "$@"
else
    "$script_dir/prod.sh" "$command" "$@"
fi
