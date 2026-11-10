#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../gatling"
for value in "${1:-10}" "${2:-10}" "${3:-40}"; do
  if ! [[ "$value" =~ ^[1-9][0-9]*$ ]]; then echo "Arguments must be positive integers" >&2; exit 1; fi
done
if (( ${3:-40} <= ${2:-10} )); then echo "Duration must exceed ramp-up" >&2; exit 1; fi
cmp ../data/products.csv src/test/resources/products.csv
exec mvn -B test-compile gatling:test -Dusers="${1:-10}" -Dramp="${2:-10}" \
  -Dduration="${3:-40}" -Dport="${PORT:-8087}" -Dgatling.runDescription="${1:-10}-users"
