#!/usr/bin/env bash
set -euo pipefail

basedir="$(cd "$(dirname "$0")" && git rev-parse --show-toplevel)"
cd "$basedir"

temp_file=$(mktemp)
trap 'rm -f "$temp_file"' EXIT

while IFS=' ' read -r search banned; do
    if [[ -z "$search" || -z "$banned" ]]; then
        continue
    fi
    echo "Checking: banning '$banned' from '$search'"
    grep -nrE "import $banned" src/main/java/"$search" 2>/dev/null | \
        sed -E 's/^(.*):([0-9]+):(.*)/::error file=\1,line=\2::Illegal import: \3/' | \
        tee -a "$temp_file" || true
done < <(grep -v '^\s*#' .github/workflows/illegal-imports.txt | grep -v '^\s*$')

found=$(wc -l < "$temp_file")
echo "# Found $found illegal import(s). Check the annotations above for details." >> "$GITHUB_STEP_SUMMARY"

if [[ "$found" -ne 0 ]]; then
    exit 1
fi

echo "All import checks passed."
