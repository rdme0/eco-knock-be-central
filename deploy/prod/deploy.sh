#!/usr/bin/env sh
set -eu

script_dir="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
repo_root="$(CDPATH= cd -- "$script_dir/../.." && pwd)"

command="${1:-up}"
if [ "$#" -gt 0 ]; then
    shift
fi

cd "$repo_root"
git pull origin main

"$script_dir/prod.sh" "$command" "$@"
