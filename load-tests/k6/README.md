# Baton k6 부하테스트 실행 안내

이 폴더에는 Baton 품질 검증을 위한 간단한 k6 시나리오가 들어 있습니다.

## 설치

```powershell
winget install k6.k6
k6 version
```

## 테스트 대상 설정

기본적으로 배포된 개발 서버를 사용합니다.

```powershell
$env:BASE_URL = "http://54.116.23.255"
```

로컬 서버를 테스트할 때는 다음 값을 사용합니다.

```powershell
$env:BASE_URL = "http://localhost:8080"
```

## 실행 순서

스모크 테스트부터 시작하고 부하를 점진적으로 높입니다.

```powershell
k6 run load-tests/k6/smoke-read.js
k6 run load-tests/k6/read-apis.js
k6 run load-tests/k6/auth-login.js
```

## 주석이 포함된 예제

`examples/annotated-read-example.js`는 `options`, `stages`, `thresholds`, `setup`, 가상 사용자, 검증 조건, 환경 변수, 요청 간 대기 시간을 한국어 주석으로 설명하는 실행 가능한 학습용 예제입니다.

```powershell
$env:EXAMPLE_VUS = "3"
$env:EXAMPLE_HOLD = "30s"
k6 run load-tests/k6/examples/annotated-read-example.js
```

## Docker Compose로 실행

Docker Compose를 사용하면 팀원이 같은 k6 버전으로 테스트할 수 있습니다. 아래 명령을 실행하기 전에 Docker Desktop을 실행해야 합니다.

```powershell
docker compose -f compose-load-test.yaml run --rm k6-smoke
docker compose -f compose-load-test.yaml run --rm k6-read
docker compose -f compose-load-test.yaml run --rm k6-auth
docker compose -f compose-load-test.yaml run --rm k6-purchase
```

환경 변수로 테스트 대상과 부하 조건을 변경할 수 있습니다.

```powershell
$env:BASE_URL = "http://host.docker.internal:8080"
$env:READ_VUS = "10"
$env:READ_HOLD = "3m"
docker compose -f compose-load-test.yaml run --rm k6-read
```

Windows 호스트에서 Spring Boot 서버를 직접 실행할 때는 `host.docker.internal`을 사용합니다. 개발 환경 테스트에는 배포 서버 주소를 사용합니다.

PURCHASE 시나리오는 사용자, 재능, 제안, 거래, 에스크로, 크레딧 원장 데이터를 생성하므로 낮은 부하로만 실행합니다.

```powershell
k6 run load-tests/k6/purchase-flow-smoke.js
```

## 환경 변수

| 이름 | 기본값 | 설명 |
| --- | --- | --- |
| `BASE_URL` | `http://54.116.23.255` | API 서버 기본 주소 |
| `SMOKE_VUS` | `1` | 스모크 테스트 가상 사용자 수 |
| `READ_VUS` | `5` | 조회 시나리오 가상 사용자 수 |
| `AUTH_VUS` | `3` | 로그인 시나리오 가상 사용자 수 |
| `PURCHASE_VUS` | `1` | PURCHASE 시나리오 가상 사용자 수 |

## 보고서 기록 항목

결과는 `docs/testing/load-test-report-template.md`에 기록합니다.

- 전체 요청 수
- 실패 요청 수
- `http_req_duration` 평균
- `http_req_duration` p95
- 상태 코드 분포
- 관측된 오류

## 문제 해결

k6가 `127.0.0.1:9` 프록시 연결 오류를 반환하면 현재 PowerShell 세션에 상속된 프록시 환경 변수를 제거합니다.

```powershell
$env:HTTP_PROXY = ""
$env:HTTPS_PROXY = ""
$env:ALL_PROXY = ""
$env:NO_PROXY = "54.116.23.255"
```

Docker Compose를 사용할 때는 Docker Desktop이 실행 중인지 먼저 확인합니다.
