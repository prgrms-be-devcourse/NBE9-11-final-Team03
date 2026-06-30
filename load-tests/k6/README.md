# Baton k6 Load Tests

This folder contains lightweight k6 scenarios for Baton QA.

## Install

```powershell
winget install k6.k6
k6 version
```

## Target

Use the deployed dev server by default:

```powershell
$env:BASE_URL = "http://54.116.23.255"
```

For local testing:

```powershell
$env:BASE_URL = "http://localhost:8080"
```

## Run Order

Start with smoke tests, then increase load gradually.

```powershell
k6 run load-tests/k6/smoke-read.js
k6 run load-tests/k6/read-apis.js
k6 run load-tests/k6/auth-login.js
```

## Detailed Annotated Example

`examples/annotated-read-example.js` is an executable learning example with
detailed Korean comments for `options`, `stages`, `thresholds`, `setup`, VUs,
checks, environment variables, and think time.

```powershell
$env:EXAMPLE_VUS = "3"
$env:EXAMPLE_HOLD = "30s"
k6 run load-tests/k6/examples/annotated-read-example.js
```

## Run with Docker Compose

Docker Compose keeps the k6 version consistent across team members. Docker
Desktop must be running before executing these commands.

```powershell
docker compose -f compose-load-test.yaml run --rm k6-smoke
docker compose -f compose-load-test.yaml run --rm k6-read
docker compose -f compose-load-test.yaml run --rm k6-auth
docker compose -f compose-load-test.yaml run --rm k6-purchase
```

Override the target server or load with environment variables:

```powershell
$env:BASE_URL = "http://host.docker.internal:8080"
$env:READ_VUS = "10"
$env:READ_HOLD = "3m"
docker compose -f compose-load-test.yaml run --rm k6-read
```

Use `host.docker.internal` when the Spring Boot server runs directly on the
Windows host. Use the deployed server URL for dev environment tests.

Run the write-heavy purchase scenario only with a low load because it creates
users, talents, proposals, trades, escrow records, and credit transactions.

```powershell
k6 run load-tests/k6/purchase-flow-smoke.js
```

## Environment Variables

| Name | Default | Description |
| --- | --- | --- |
| `BASE_URL` | `http://54.116.23.255` | API server base URL |
| `SMOKE_VUS` | `1` | smoke test virtual users |
| `READ_VUS` | `5` | read scenario virtual users |
| `AUTH_VUS` | `3` | login scenario virtual users |
| `PURCHASE_VUS` | `1` | purchase scenario virtual users |

## Report Values

Record these values in `docs/testing/load-test-report-template.md`.

- total requests
- failed requests
- `http_req_duration` average
- `http_req_duration` p95
- status code distribution
- observed errors

## Troubleshooting

If k6 reports a proxy connection to `127.0.0.1:9`, clear inherited proxy
variables for the current PowerShell session before running the test:

```powershell
$env:HTTP_PROXY = ""
$env:HTTPS_PROXY = ""
$env:ALL_PROXY = ""
$env:NO_PROXY = "54.116.23.255"
```

When using Docker Compose, make sure Docker Desktop is running first.
