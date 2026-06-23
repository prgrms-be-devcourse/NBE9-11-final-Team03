# Baton 시연 스크립트

> 문서 버전: v1.3
> 기준일: 2026-06-23
> 기준 브랜치: `dev`
> 기준 PR: `#62`, `#63`, `#64`, `#67`, `#68` 반영 기준
> 문서 상태: MVP 수동 API 테스트 결과 반영
> 목적: Swagger/Postman API 시연과 UI 시연을 발표 중 끊기지 않게 진행하기 위한 순서 문서

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-20 | 최초 시연 스크립트 작성 | 작성 완료 |
| v1.1 | 2026-06-22 | 문서 버전/기준 브랜치/문서 상태 추가 | 구현 반영 필요 |
| v1.2 | 2026-06-22 | `@CurrentUser` 인증 사용자 기준, Trade/Submission/S3 API 반영 | 최신 구현 기준 요약 |
| v1.3 | 2026-06-23 | 초기 크레딧 자동 지급, CreditTransaction 조회, 구매 확정 재조회 이슈 반영 | 최신 테스트 기준 |

## 1. 시연 원칙

- 메인 시연은 PURCHASE MVP 흐름만 사용한다.
- 고도화 기능은 S3 첨부파일과 채팅을 별도 보조 시연 또는 확장 계획으로 분리한다.
- 시연 전 구매자/제공자 계정, `talentId`, `proposalId`, `tradeId`를 기록한다.
- 검증되지 않은 기능은 발표 중 즉석으로 시도하지 않는다.
- 배포 환경이 불안정하면 로컬 Swagger를 대체 경로로 사용한다.

## 2. 사전 준비

| 항목 | 값 | 상태 |
| --- | --- | --- |
| Local Swagger | `http://localhost:8080/swagger-ui/index.html` | 준비 |
| Local OpenAPI | `http://localhost:8080/v3/api-docs` | 준비 |
| 배포 Swagger | 확인 필요 | 배포 후 갱신 |
| 구매자 계정 | 신규 생성 또는 init data | 준비 필요 |
| 제공자 계정 | 신규 생성 또는 init data | 준비 필요 |
| 카테고리 ID | init data 또는 DB 확인 | 준비 필요 |
| 초기 크레딧 | 신규 가입 직후 `10000` | 구현/검증 완료 |

## 3. API 시연 흐름

### 3.1 회원가입

Endpoint: `POST /api/v1/auth/signup`

구매자와 제공자를 각각 생성한다.

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| HTTP status | 201 |
| 사용자 ID | 응답 `data.id` 기록 |
| 사용자 상태 | `ACTIVE` |

주의: 회원가입 후 바로 `/api/v1/credit/balance`를 조회해 초기 크레딧 `10000`, `escrowBalance=0`을 먼저 보여준다.

### 3.2 로그인

Endpoint: `POST /api/v1/auth/login`

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| accessToken | 응답 body |
| refreshToken | HttpOnly cookie |

이후 인증이 필요한 API는 로그인한 사용자의 access token을 사용한다.

### 3.3 크레딧 잔액 확인

Endpoint: `GET /api/v1/credit/balance`

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| balance | 초기 지급 크레딧 |
| escrowBalance | 0 |

### 3.4 제공자 재능 등록

Endpoint: `POST /api/v1/talents`

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| HTTP status | 201 |
| Location header | `/api/v1/talents/{talentId}` |
| talentId | 응답 `data.talentId` 기록 |

### 3.5 재능 목록/상세 조회

Endpoint:

- `GET /api/v1/talents`
- `GET /api/v1/talents/{talentId}`
- `GET /api/v1/talents/search`

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| 목록 | 활성 재능 노출 |
| 상세 | 제목, 내용, 가격, 작성자 정보 |
| 검색 | 조건에 맞는 재능 반환 |

### 3.6 매칭 제안 생성

Endpoint: `POST /api/v1/match-proposals`

현재 로그인한 사용자가 요청자가 된다.

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| HTTP status | 201 |
| status | `REQUESTED` |
| proposalId | 응답 ID 기록 |

### 3.7 매칭 제안 수락

Endpoint: `PATCH /api/v1/match-proposals/{proposalId}/accept`

Header:

```text
Idempotency-Key: accept-proposal-{proposalId}
```

현재 로그인한 제공자가 제안을 수락한다.

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| proposal status | `ACCEPTED` |
| Trade | PURCHASE 또는 정책상 tradeType 생성 |
| Credit | 구매자 balance 감소, escrowBalance 증가 |
| Escrow | `HELD` 생성 |
| CreditTransaction | `ESCROW_HOLD` 기록 |

### 3.8 거래 조회

Endpoint: `GET /api/v1/trade/{tradeId}`

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| status | `IN_PROGRESS` |
| buyer/seller | 구매자/제공자 ID 일치 |
| escrow | HELD 상태 확인 |

### 3.9 결과물 제출

Endpoint:

- `POST /api/v1/trade/{tradeId}/submission/presigned-url`
- `POST /api/v1/trade/{tradeId}/submission`

제공자가 S3 업로드 URL을 발급받고, 업로드한 결과물의 key 또는 설명을 제출한다.

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| submission | 결과물 기록 생성 |
| trade status | `UNDER_REVIEW` |

### 3.10 결과물 조회 및 구매 확정

Endpoint:

- `GET /api/v1/trade/{tradeId}/submission`
- `PATCH /api/v1/trade/{tradeId}/confirm`

구매자가 결과물을 확인하고 구매 확정을 호출한다.

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| trade status | `COMPLETED` |
| escrow status | `RELEASED` |
| buyer escrowBalance | 감소 |
| seller balance | 증가 |
| CreditTransaction | `ESCROW_RELEASE` 기록 |

주의: 2026-06-23 수동 테스트에서 구매 확정 응답은 `COMPLETED/RELEASED`였지만, 직후 거래 상세 재조회가 `UNDER_REVIEW/HELD`로 남는 현상이 관측되었다. 발표 전 이 이슈가 해결되었는지 다시 확인한다.

### 3.11 크레딧 거래 내역 조회

Endpoint: `GET /api/v1/credit/transactions`

선택 Query:

- `type`: `WELCOME`, `ESCROW_HOLD`, `ESCROW_RELEASE`, `REFUND` 등
- `from`: ISO DateTime
- `to`: ISO DateTime
- `cursor`: 마지막으로 조회한 거래 원장 ID
- `size`: 조회 개수

확인 포인트:

| 확인 | 기대값 |
| --- | --- |
| 구매자 내역 | `ESCROW_HOLD`, `ESCROW_RELEASE` 기록 |
| 제공자 내역 | `ESCROW_RELEASE` 정산 기록 |
| balanceAfter | 각 거래 이후 잔액 반영 |

## 4. 실패 시 대체 기준

| 실패 지점 | 대체 설명 |
| --- | --- |
| 회원가입 후 초기 크레딧 없음 | 최신 dev와 실행 서버가 같은지 확인하고, 필요 시 서버 재기동 후 재시도 |
| 배포 Swagger 불안정 | 로컬 Swagger로 전환 |
| S3 업로드 실패 | presigned URL 발급과 DB 제출 흐름을 분리해 설명 |
| CreditTransaction 조회 실패 | `/api/v1/credit/transactions` 응답 또는 DB에서 이력 기록 확인 |
| 구매 확정 후 재조회 상태 불일치 | confirm 응답, 잔액, CreditTransaction 정산 내역을 근거로 보여주고 구현 이슈로 분리 |

## 5. 최종 시연 체크리스트

- [ ] 구매자/제공자 로그인 토큰 준비
- [ ] 회원가입 후 초기 크레딧 자동 지급 확인
- [ ] 제공자 재능 등록 확인
- [ ] 매칭 제안 생성 확인
- [ ] 매칭 제안 수락 후 Trade/Credit/Escrow 연결 확인
- [ ] 결과물 제출 확인
- [ ] 구매 확정 후 정산 확인
- [ ] `/api/v1/credit/transactions`로 크레딧 변동 이력 확인
