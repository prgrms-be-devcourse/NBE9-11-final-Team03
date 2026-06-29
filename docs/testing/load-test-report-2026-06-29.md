# Baton k6 Initial Load Test Report

> Date: 2026-06-29
> Branch: `chore/BATON-148-k6-load-test`
> Target: `http://54.116.23.255`
> Tool: k6 v2.0.0

## 1. Purpose

Validate that the initial k6 scenarios can execute against the deployed Baton
dev server and establish baseline latency and failure-rate measurements.

## 2. Pass Criteria

| Metric | Target |
| --- | ---: |
| HTTP failure rate | `< 1%` |
| Read API p95 | `< 1000ms` |
| Login API p95 | `< 1500ms` |
| Purchase flow p95 | `< 2000ms` |

## 3. Results

| Scenario | Load | Requests | Failure rate | Average | p95 | Maximum | Result |
| --- | --- | ---: | ---: | ---: | ---: | ---: | --- |
| smoke-read | 1 VU, 10s | 35 | 0% | 51.15ms | 125.01ms | 442.32ms | Pass |
| read-apis | up to 5 VUs, 70s | 1,151 | 0% | 22.76ms | 32.61ms | 215.53ms | Pass |
| auth-login | up to 3 VUs, 70s | 125 | 0% | 106.36ms | 115.12ms | 315.87ms | Pass |
| purchase-flow-smoke | 1 VU, 1 iteration | 12 | 0% | 103.60ms | 220.89ms | 338.02ms | Pass |

## 4. Verified Behavior

- Read APIs returned successful `ApiResponse` values under the initial load.
- Repeated login completed without HTTP failures.
- The purchase flow completed signup, login, talent creation, proposal accept,
  trade creation, submission, purchase confirmation, and balance checks.
- All configured k6 thresholds passed.

## 5. Environment Notes

- Docker Compose configuration validation passed.
- Docker-based execution was not completed because the local Docker Desktop
  engine was not running and its Windows service could not be started from the
  current session.
- The same scripts were executed with the locally installed k6 binary.
- Proxy variables inherited by the shell initially pointed to `127.0.0.1:9`;
  clearing them allowed direct access to the dev server.

## 6. Next Test

Run the read scenario at 10 and 20 VUs while collecting server CPU, memory,
database connection, and error-log evidence. Keep the purchase flow at a low
iteration count because it creates persistent test data.
