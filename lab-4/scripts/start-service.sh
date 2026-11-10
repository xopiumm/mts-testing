#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
exec java -Dport="${PORT:-8087}" service/LocalApi.java
