#!/usr/bin/env python3
import csv
import json
import math
from pathlib import Path

root = Path(__file__).resolve().parents[1]
print("| Tool / users | Requests | RPS | Average, ms | p95, ms | Errors, % |")
print("|---|---:|---:|---:|---:|---:|")
for users in (10, 25, 50):
    path = root / f"results/jmeter-{users}/results.jtl"
    if not path.exists():
        continue
    rows = list(csv.DictReader(path.open()))
    values = sorted(int(row["elapsed"]) for row in rows)
    seconds = (max(int(row["timeStamp"]) + int(row["elapsed"]) for row in rows)
               - min(int(row["timeStamp"]) for row in rows)) / 1000
    errors = sum(row["success"] != "true" for row in rows)
    print(f"| JMeter / {users} | {len(rows)} | {len(rows)/seconds:.2f} | {sum(values)/len(values):.2f} | {values[math.ceil(.95*len(values))-1]} | {100*errors/len(rows):.2f} |")
for users in (10, 25, 50):
    path = root / f"results/gatling-{users}/js/stats.js"
    if not path.exists():
        continue
    content = path.read_text()
    stats, _ = json.JSONDecoder().raw_decode(content.split("stats: ", 1)[1])
    print(f"| Gatling / {users} | {stats['numberOfRequests']['total']} | {stats['meanNumberOfRequestsPerSecond']['total']} | {stats['meanResponseTime']['total']} | {stats['percentiles3']['total']} | {100*int(stats['numberOfRequests']['ko'])/int(stats['numberOfRequests']['total']):.2f} |")
