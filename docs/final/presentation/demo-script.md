# Baton 시연 스크립트

> 문서 버전: v1.2  
> 기준일: 2026-06-22  
> 기준 브랜치: `refactor/BATON-88-current-user`  
> 기준 PR: `#63` 참고  
> 문서 상태: 최신 구현 기준 요약  
> 목적: Swagger/Postman API 시연과 UI 시연을 발표 중 끊기지 않게 진행하기 위한 순서 문서

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-20 | 최초 시연 스크립트 작성 | 작성 완료 |
| v1.1 | 2026-06-22 | 문서 버전/기준 브랜치/문서 상태 추가 | 구현 반영 필요 |
| v1.2 | 2026-06-22 | `@CurrentUser` 인증 사용자 기준, Trade/Submission/S3 API 반영 | 최신 구현 기준 요약 |

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
| 초기 크레딧 | 정책 기준 값 | 회원가입 자동 지급 연결 필요 |

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

주의: 현재 `AuthService.signup()`에는 Account 생성 TODO가 남아 있다. 신규 사용자 기준 시연을 하려면 회원가입 후 초기 크레딧 자동 지급 연결이 먼저 필요하다.

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

## 4. 실패 시 대체 기준

| 실패 지점 | 대체 설명 |
| --- | --- |
| 회원가입 후 초기 크레딧 없음 | 초기 계좌 자동 생성 연결이 P0 잔여 작업임을 설명하고 init data 또는 직접 계좌 준비로 시연 |
| 배포 Swagger 불안정 | 로컬 Swagger로 전환 |
| S3 업로드 실패 | presigned URL 발급과 DB 제출 흐름을 분리해 설명 |
| CreditTransaction 조회 API 없음 | 기록 로직은 존재하되 시연 확인 방식은 DB 또는 후속 API로 분리 |

## 5. 최종 시연 체크리스트

- [ ] 구매자/제공자 로그인 토큰 준비
- [ ] 회원가입 후 초기 크레딧 자동 지급 확인
- [ ] 제공자 재능 등록 확인
- [ ] 매칭 제안 생성 확인
- [ ] 매칭 제안 수락 후 Trade/Credit/Escrow 연결 확인
- [ ] 결과물 제출 확인
- [ ] 구매 확정 후 정산 확인
- [ ] 크레딧 변동 이력 확인 방식 확정
