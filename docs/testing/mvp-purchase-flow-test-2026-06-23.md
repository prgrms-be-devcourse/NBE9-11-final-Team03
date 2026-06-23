# MVP PURCHASE 정상 흐름 테스트 기록

기준일: 2026-06-23  
기준 브랜치: `dev`  
테스트 방식: `localhost:8080` 기준 실제 HTTP API 수동 호출  
테스트 목적: 회원가입부터 PURCHASE 거래 완료, 에스크로 정산, 크레딧 변동 내역 조회까지 MVP 핵심 정상 흐름이 하나로 이어지는지 확인한다.

## 1. 결론

| 항목 | 결과 |
|---|---|
| MVP 정상 흐름 통과 여부 | 통과 |
| 회원가입/로그인 | 성공 |
| 초기 크레딧 지급 | 성공 |
| 재능 등록 | 성공 |
| 매칭 제안 생성/수락 | 성공 |
| PURCHASE Trade 생성 | 성공 |
| 구매자 크레딧 에스크로 보관 | 성공 |
| 결과물 제출 후 검토 상태 전환 | 성공 |
| 구매 확정/정산 | 성공 |
| CreditTransaction 기록 조회 | 성공 |

최종 판단: `Auth -> Credit -> Talent -> Matching -> Trade -> Escrow -> CreditTransaction` 정상 흐름은 API 기준으로 재현 가능하다.

## 2. 테스트 환경

| 항목 | 내용 |
|---|---|
| 실행 URL | `http://localhost:8080` |
| 실행 명령 | `.\gradlew.bat bootRun` |
| API 문서 확인 | `GET /v3/api-docs` 200 응답 |
| 인증 방식 | 로그인 후 발급된 JWT accessToken을 `Authorization: Bearer {token}`으로 전달 |
| 테스트 데이터 | 신규 구매자/제공자 계정 생성 |

## 3. 테스트 데이터

| 구분 | 값 |
|---|---|
| 구매자 ID | `12` |
| 제공자 ID | `13` |
| 재능 ID | `1` |
| 매칭 제안 ID | `1` |
| 거래 ID | `1` |
| 에스크로 ID | `1` |
| 거래 금액 | `5000` |
| 초기 크레딧 | `10000` |

주의: 위 ID는 2026-06-23 로컬 테스트 실행 시 생성된 값이다. DB 초기화 상태에 따라 재실행 시 ID는 달라질 수 있다.

## 4. 정상 흐름 결과

### 4.1 회원가입

구매자와 제공자를 신규 가입시켰다.

| 사용자 | 결과 코드 | 생성 ID |
|---|---|---:|
| 구매자 | `201-1` | `12` |
| 제공자 | `201-1` | `13` |

검증 결과:
- 회원가입 API가 성공했다.
- 회원가입 이후 초기 크레딧 계좌가 생성되었다.
- `WELCOME` 크레딧 내역이 생성되었다.

### 4.2 로그인

구매자와 제공자 모두 로그인 후 accessToken 발급을 확인했다.

| 사용자 | 결과 |
|---|---|
| 구매자 | accessToken 발급 성공 |
| 제공자 | accessToken 발급 성공 |

### 4.3 초기 크레딧 확인

Endpoint: `GET /api/v1/credit/balance`

| 사용자 | balance | escrowBalance |
|---|---:|---:|
| 구매자 | `10000` | `0` |
| 제공자 | `10000` | `0` |

검증 결과:
- 회원가입 직후 초기 크레딧 지급이 동작한다.
- 구매자와 제공자 모두 사용 가능 크레딧과 에스크로 크레딧이 정상 초기화된다.

### 4.4 재능 등록

Endpoint: `POST /api/v1/talents`

요청 핵심 값:

```json
{
  "categoryId": 1,
  "title": "MVP 백엔드 테스트 재능",
  "content": "Spring Boot API 구현을 도와드립니다.",
  "estimatedHours": 3,
  "creditPrice": 5000
}
```

결과:

| 항목 | 값 |
|---|---:|
| talentId | `1` |
| creditPrice | `5000` |

### 4.5 매칭 제안 생성

Endpoint: `POST /api/v1/match-proposals`

요청 핵심 값:

```json
{
  "requesterTalentId": null,
  "providerId": 13,
  "providerTalentId": 1,
  "requestMessage": "MVP PURCHASE 테스트 요청입니다."
}
```

결과:

| 항목 | 값 |
|---|---|
| proposalId | `1` |
| status | `REQUESTED` |
| requesterId | `12` |
| providerId | `13` |

검증 결과:
- PURCHASE 흐름에서는 `requesterTalentId`가 `null`이어도 제안 생성이 가능하다.
- 요청자는 토큰에서 가져온 구매자 ID로 저장된다.

### 4.6 매칭 제안 수락

Endpoint: `PATCH /api/v1/match-proposals/{proposalId}/accept`

요청 조건:
- 제공자 accessToken 사용
- `Idempotency-Key` 헤더 전달

결과:

| 항목 | 값 |
|---|---|
| proposalId | `1` |
| status | `ACCEPTED` |

검증 결과:
- 매칭 제안 상태가 `ACCEPTED`로 변경되었다.
- PURCHASE 거래가 생성되었다.
- 구매자 크레딧이 에스크로로 보관되었다.
- Escrow가 `HELD` 상태로 생성되었다.

### 4.7 수락 후 크레딧 확인

Endpoint: `GET /api/v1/credit/balance`

| 사용자 | balance | escrowBalance |
|---|---:|---:|
| 구매자 | `5000` | `5000` |
| 제공자 | `10000` | `0` |

검증 결과:
- 구매자의 사용 가능 크레딧이 `10000 -> 5000`으로 감소했다.
- 구매자의 에스크로 보관 크레딧이 `0 -> 5000`으로 증가했다.
- 제공자 크레딧은 아직 정산 전이므로 변하지 않았다.

### 4.8 거래 상태 조회

Endpoint: `GET /api/v1/trade/{tradeId}`

결과:

| 항목 | 값 |
|---|---|
| tradeId | `1` |
| matchId | `1` |
| buyerId | `12` |
| sellerId | `13` |
| creditPrice | `5000` |
| tradeStatus | `IN_PROGRESS` |
| escrowStatus | `HELD` |

검증 결과:
- MatchProposal 수락 후 Trade가 생성되었다.
- Trade와 Escrow가 정상 연결되어 조회된다.

주의:
- 현재 테스트에서는 거래 목록 API가 없어서 `GET /api/v1/trade/{tradeId}`를 ID 범위로 조회해 `matchId == proposalId`인 거래를 확인했다.
- Swagger/Postman 시연에서는 매칭 수락 응답에 `tradeId`가 없으므로 거래 ID 확인 방법을 별도로 정해야 한다.

### 4.9 결과물 제출

Endpoint: `POST /api/v1/trade/{tradeId}/submission`

요청 핵심 값:

```json
{
  "fileKey": "trades/1/mvp-result.txt",
  "description": "MVP 테스트 결과물 제출입니다."
}
```

결과:

| 항목 | 값 |
|---|---:|
| submissionId | `1` |
| escrowId | `1` |

제출 후 거래 상태:

| 항목 | 값 |
|---|---|
| tradeStatus | `UNDER_REVIEW` |
| escrowStatus | `HELD` |

검증 결과:
- 제공자가 결과물을 제출하면 거래 상태가 `UNDER_REVIEW`로 변경된다.
- 구매 확정 가능 상태로 진입한다.

### 4.10 구매 확정 및 정산

Endpoint: `PATCH /api/v1/trade/{tradeId}/confirm`

결과:

| 항목 | 값 |
|---|---|
| tradeId | `1` |
| tradeStatus | `COMPLETED` |
| escrowStatus | `RELEASED` |

최종 크레딧:

| 사용자 | balance | escrowBalance |
|---|---:|---:|
| 구매자 | `5000` | `0` |
| 제공자 | `15000` | `0` |

검증 결과:
- 구매자 에스크로 보관 크레딧이 `5000 -> 0`으로 감소했다.
- 제공자 사용 가능 크레딧이 `10000 -> 15000`으로 증가했다.
- Trade가 `COMPLETED` 상태가 되었다.
- Escrow가 `RELEASED` 상태가 되었다.

### 4.11 CreditTransaction 조회

Endpoint: `GET /api/v1/credit/transactions?size=10`

구매자 내역:

| type | amount | balanceAfter | relatedTradeId |
|---|---:|---:|---:|
| `ESCROW_RELEASE` | `-5000` | `5000` | `1` |
| `ESCROW_HOLD` | `-5000` | `5000` | `1` |
| `WELCOME` | `10000` | `10000` |  |

제공자 내역:

| type | amount | balanceAfter | relatedTradeId |
|---|---:|---:|---:|
| `ESCROW_RELEASE` | `5000` | `15000` | `1` |
| `WELCOME` | `10000` | `10000` |  |

검증 결과:
- 초기 지급 내역이 기록된다.
- 에스크로 보류 내역이 기록된다.
- 구매 확정 후 구매자/제공자 양쪽 정산 내역이 기록된다.

## 5. 발견 사항

### P0

현재 정상 흐름 기준 P0 차단 이슈는 없다.

### P1

| 항목 | 내용 | 조치 |
|---|---|---|
| 거래 ID 확인 방식 | 매칭 수락 응답에 `tradeId`가 포함되지 않아 Swagger/Postman 시연 중 다음 거래 API 호출에 사용할 ID를 바로 알기 어렵다. | 수락 응답에 `tradeId` 포함 또는 거래 목록/상세 조회 시연 방식 정리 |
| 재능 상세 조회 인증 정책 | 비로그인 `GET /api/v1/talents/{talentId}` 호출 시 403이 발생했다. | 재능 조회를 공개 API로 둘지, 로그인 필요 API로 둘지 결정 후 Swagger 설명과 일치시킨다. |
| 실패 케이스 미검증 | 이번 문서는 정상 흐름 검증만 포함한다. | 크레딧 부족, 중복 수락, 권한 없음, 재확정, idempotency 재사용을 별도 테스트한다. |

### P2

| 항목 | 내용 |
|---|---|
| 테스트 기록 자동화 | 현재는 수동 API 호출 결과를 기록했다. 추후 Postman Collection 또는 E2E 통합 테스트로 자동화할 수 있다. |
| 결과물 파일 흐름 | 이번 테스트는 `fileKey`를 직접 전달했다. 실제 S3 업로드까지 포함한 파일 흐름은 별도 검증이 필요하다. |

## 6. MVP 판단

이번 테스트 기준으로 Baton의 PURCHASE MVP 정상 흐름은 다음 조건을 만족한다.

- 신규 사용자가 회원가입 후 초기 크레딧을 받는다.
- 제공자가 재능을 등록한다.
- 구매자가 매칭 제안을 생성한다.
- 제공자가 제안을 수락하면 PURCHASE 거래와 Escrow가 생성된다.
- 구매자 크레딧은 사용 가능 잔액에서 차감되고 에스크로 잔액으로 이동한다.
- 제공자가 결과물을 제출하면 거래가 검토 상태가 된다.
- 구매자가 확정하면 거래가 완료되고 에스크로가 해제된다.
- 제공자에게 크레딧이 정산된다.
- 구매자와 제공자의 CreditTransaction 내역이 기록되고 조회된다.

최종 판단: MVP 정상 흐름은 API 기준으로 통과했다. 남은 검증은 실패 케이스와 시연 편의성 보강이다.
