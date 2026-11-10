#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
for value in "${1:-10}" "${2:-10}" "${3:-40}"; do
  if ! [[ "$value" =~ ^[1-9][0-9]*$ ]]; then echo "Arguments must be positive integers" >&2; exit 1; fi
done
if (( ${3:-40} <= ${2:-10} )); then echo "Duration must exceed ramp-up" >&2; exit 1; fi
users=${1:-10}
ramp=${2:-10}
duration=${3:-40}
name=${4:-jmeter-${users}}
out="${RESULTS_DIR:-results}/$name"
mkdir -p "$out"
if [ -e "$out/results.jtl" ]; then echo "Results already exist: $out" >&2; exit 1; fi
"${JMETER_HOME:?Set JMETER_HOME to Apache JMeter 5.6.3}/bin/jmeter" -n -t products.jmx \
  -Jusers="$users" -Jramp="$ramp" -Jduration="$duration" -Jport="${PORT:-8087}" \
  -Jjmeter.save.saveservice.output_format=csv \
  -l "$out/results.jtl" -j "$out/jmeter.log" -e -o "$out/report"
