# Baton MVP API 테스트 요청/응답 기록

> 문서 버전: v1.1  
> 기준일: 2026-06-23  
> 기준 브랜치: `dev`  
> 실행 환경: `localhost:8080`  
> 목적: PURCHASE MVP 흐름을 Swagger/Postman에서 재현할 수 있도록 실제 요청, 실제 응답, 결과, 비고를 남긴다.

## 1. 테스트 데이터

| 항목 | 값 |
| --- | --- |
| 테스트 일시 | 2026-06-23 12:44~12:45 KST |
| Swagger URL | `http://localhost:8080/swagger-ui/index.html` |
| API Docs | `http://localhost:8080/v3/api-docs` |
| 구매자 email | `buyer124404@test.com` |
| 제공자 email | `seller124404@test.com` |
| 구매자 비밀번호 | `<test-password>` |
| 제공자 비밀번호 | `<test-password>` |
| buyerId | `14` |
| sellerId | `15` |
| talentId | `2` |
| proposalId | `2` |
| tradeId | `2` |

주의:
- accessToken, 비밀번호, presigned URL query string은 민감값이므로 마스킹했다.
- PowerShell 콘솔 인코딩 문제로 한글 응답 메시지가 깨져 출력되어, 문서에는 API 의미에 맞는 정상 한글 문구로 정리했다.

## 2. 최종 요약

| 단계 | 결과 | 비고 |
| --- | --- | --- |
| 회원가입/로그인 | 성공 | 구매자 `14`, 제공자 `15` 생성 및 로그인 성공 |
| 초기 크레딧 지급 | 성공 | 신규 가입 직후 구매자 balance `10000`, escrowBalance `0` |
| 재능 등록 | 성공 | 제공자 재능 `talentId=2` 생성 |
| 재능 조회 | 부분 이슈 | 인증 없이 조회하면 403, Bearer Token 포함 시 조회 성공 |
| 매칭 제안 생성/수락 | 성공 | `proposalId=2`, 상태 `REQUESTED -> ACCEPTED` |
| PURCHASE 거래 생성 | 성공 | `tradeId=2`, `IN_PROGRESS`, Escrow `HELD` |
| 구매자 크레딧 보류 | 성공 | 구매자 거래내역 `ESCROW_HOLD -100`, balanceAfter `9900` |
| 결과물 제출/조회 | 성공 | submission `id=2`, escrowId `2` |
| 구매 확정/정산 | 부분 이슈 | 확정 응답은 `COMPLETED/RELEASED`, 직후 거래 재조회는 `UNDER_REVIEW/HELD`로 불일치 |
| 제공자 정산 | 성공 | 제공자 balance `10100`, `ESCROW_RELEASE +100` |
| 크레딧 변동 내역 | 성공 | WELCOME, ESCROW_HOLD, ESCROW_RELEASE 기록 확인 |

## 3. 단계별 요청/응답

### 3.1 구매자 회원가입

Request:

```http
POST /api/v1/auth/signup
Content-Type: application/json
```

```json
{
  "email": "buyer124404@test.com",
  "password": "<test-password>",
  "nickname": "buy124404",
  "profileImageUrl": null,
  "introduction": "재능을 구매하고 싶은 사용자입니다."
}
```

실제 응답:

```json
{
  "success": true,
  "code": "201-1",
  "message": "회원가입에 성공했습니다.",
  "data": {
    "id": 14,
    "email": "buyer124404@test.com",
    "nickname": "buy124404",
    "profileImageUrl": null,
    "introduction": "재능을 구매하고 싶은 사용자입니다.",
    "status": "ACTIVE",
    "role": "USER",
    "createdAt": "2026-06-23T12:44:05.2205848"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 회원가입 성공 | 성공 | `success=true`, `code=201-1` |
| buyerId 기록 | 성공 | `buyerId=14` |

### 3.2 제공자 회원가입

Request:

```http
POST /api/v1/auth/signup
Content-Type: application/json
```

```json
{
  "email": "seller124404@test.com",
  "password": "<test-password>",
  "nickname": "sel124404",
  "profileImageUrl": null,
  "introduction": "Spring 코드 리뷰를 제공하는 사용자입니다."
}
```

실제 응답:

```json
{
  "success": true,
  "code": "201-1",
  "message": "회원가입에 성공했습니다.",
  "data": {
    "id": 15,
    "email": "seller124404@test.com",
    "nickname": "sel124404",
    "profileImageUrl": null,
    "introduction": "Spring 코드 리뷰를 제공하는 사용자입니다.",
    "status": "ACTIVE",
    "role": "USER",
    "createdAt": "2026-06-23T12:44:05.5738567"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 회원가입 성공 | 성공 | `success=true`, `code=201-1` |
| sellerId 기록 | 성공 | `sellerId=15` |

### 3.3 구매자 로그인

Request:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "buyer124404@test.com",
  "password": "<test-password>"
}
```

실제 응답:

```json
{
  "success": true,
  "code": "200-1",
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJ...<masked>"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| accessToken 발급 | 성공 | 이후 구매자 인증 API에 사용 |

### 3.4 제공자 로그인

Request:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "seller124404@test.com",
  "password": "<test-password>"
}
```

실제 응답:

```json
{
  "success": true,
  "code": "200-1",
  "message": "로그인에 성공했습니다.",
  "data": {
    "accessToken": "eyJ...<masked>"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| accessToken 발급 | 성공 | 이후 제공자 인증 API에 사용 |

### 3.5 구매자 초기 크레딧 확인

Request:

```http
GET /api/v1/credit/balance
Authorization: Bearer {buyerAccessToken}
```

실제 응답:

```json
{
  "success": true,
  "code": "200-0",
  "message": "요청에 성공했습니다.",
  "data": {
    "userId": 14,
    "balance": 10000,
    "escrowBalance": 0
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| CreditAccount 생성 | 성공 | 신규 회원가입 직후 조회 성공 |
| 초기 balance | 성공 | `10000` |
| escrowBalance | 성공 | `0` |

### 3.6 제공자 재능 등록

Request:

```http
POST /api/v1/talents
Authorization: Bearer {sellerAccessToken}
Content-Type: application/json
```

```json
{
  "categoryId": 1,
  "title": "Spring Boot 코드 리뷰",
  "content": "Spring Boot 프로젝트 구조와 테스트 코드를 리뷰합니다.",
  "estimatedHours": 2,
  "creditPrice": 100
}
```

실제 응답:

```json
{
  "success": true,
  "code": "201-2",
  "message": "재능 등록에 성공했습니다.",
  "data": {
    "talentId": 2
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 재능 등록 | 성공 | `talentId=2` |
| 가격 기록 | 성공 | 이후 거래 가격 `100`으로 반영 |

### 3.7 재능 목록/상세 조회

Request 1:

```http
GET /api/v1/talents
```

Request 2:

```http
GET /api/v1/talents/2
```

인증 없이 호출한 실제 응답:

```text
HTTP 403 Forbidden
```

Bearer Token 포함 재호출 실제 응답:

```json
{
  "success": true,
  "code": "200-2",
  "message": "재능 요청에 성공했습니다.",
  "data": {
    "content": [
      {
        "talentId": 2,
        "categoryName": "개발",
        "title": "Spring Boot 코드 리뷰",
        "creditPrice": 100,
        "estimatedHours": 2,
        "avgRating": 0.0,
        "completeCount": 0,
        "viewCount": 0,
        "createdAt": "2026-06-23T12:44:05.952803"
      }
    ],
    "hasNext": false,
    "nextCursor": 1
  }
}
```

상세 조회 실제 응답:

```json
{
  "success": true,
  "code": "200-2",
  "message": "재능 요청에 성공했습니다.",
  "data": {
    "id": 2,
    "categoryId": 1,
    "categoryName": "개발",
    "title": "Spring Boot 코드 리뷰",
    "content": "Spring Boot 프로젝트 구조와 테스트 코드를 리뷰합니다.",
    "estimatedHours": 2,
    "creditPrice": 100,
    "status": "ACTIVE",
    "author": {
      "authorId": 15,
      "nickname": "sel124404",
      "trustScore": 50.0
    }
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 목록에서 재능 노출 | 성공 | 인증 포함 시 `talentId=2` 확인 |
| 상세 조회 성공 | 성공 | 인증 포함 시 성공 |
| 공개 조회 정책 | 이슈 | 문서상 공개 조회라면 Security 설정 또는 API 명세 수정 필요 |

### 3.8 매칭 제안 생성

Request:

```http
POST /api/v1/match-proposals
Authorization: Bearer {buyerAccessToken}
Content-Type: application/json
```

```json
{
  "requesterTalentId": null,
  "providerId": 15,
  "providerTalentId": 2,
  "requestMessage": "Spring Boot 코드 리뷰를 구매하고 싶습니다."
}
```

실제 응답:

```json
{
  "success": true,
  "code": "201-3",
  "message": "매칭 제안이 생성되었습니다.",
  "data": {
    "id": 2,
    "providerTalentId": 2,
    "requesterTalentId": null,
    "requesterId": 14,
    "providerId": 15,
    "status": "REQUESTED",
    "requestMessage": "Spring Boot 코드 리뷰를 구매하고 싶습니다.",
    "respondedAt": null,
    "createdAt": "2026-06-23T12:44:06.0690855",
    "updatedAt": "2026-06-23T12:44:06.0690855"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 매칭 제안 생성 | 성공 | `proposalId=2` |
| PURCHASE 제안 조건 | 성공 | `requesterTalentId=null` |

### 3.9 매칭 제안 수락

Request:

```http
PATCH /api/v1/match-proposals/2/accept
Authorization: Bearer {sellerAccessToken}
Idempotency-Key: accept-proposal-2
```

실제 응답:

```json
{
  "success": true,
  "code": "200-4",
  "message": "매칭 제안을 수락했습니다.",
  "data": {
    "id": 2,
    "providerTalentId": 2,
    "requesterTalentId": null,
    "requesterId": 14,
    "providerId": 15,
    "status": "ACCEPTED",
    "respondedAt": "2026-06-23T12:44:06.1913646",
    "createdAt": "2026-06-23T12:44:06.069086",
    "updatedAt": "2026-06-23T12:44:06.069086"
  }
}
```

수락 직후 구매자 크레딧 거래내역:

```json
{
  "success": true,
  "code": "200-0",
  "data": {
    "content": [
      {
        "transactionId": 18,
        "relatedTradeId": 2,
        "type": "ESCROW_HOLD",
        "amount": -100,
        "balanceAfter": 9900,
        "defaultReason": "거래 완료까지 크레딧 에스크로 예치",
        "createdAt": "2026-06-23T12:44:06.170945"
      },
      {
        "transactionId": 16,
        "relatedTradeId": null,
        "type": "WELCOME",
        "amount": 10000,
        "balanceAfter": 10000,
        "defaultReason": "신규 가입 웰컴 크레딧 지급",
        "createdAt": "2026-06-23T12:44:05.382197"
      }
    ],
    "hasNext": false,
    "nextCursor": 16
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| proposal ACCEPTED | 성공 | `REQUESTED -> ACCEPTED` |
| Trade 생성 | 성공 | 다음 단계에서 `tradeId=2` 조회됨 |
| 구매자 balance 감소 | 성공 | `10000 -> 9900` |
| ESCROW_HOLD 내역 | 성공 | `transactionId=18`, `amount=-100` |

### 3.10 거래 조회

Request:

```http
GET /api/v1/trade/2
Authorization: Bearer {buyerAccessToken}
```

실제 응답:

```json
{
  "success": true,
  "code": "200-7",
  "message": "거래 조회에 성공했습니다.",
  "data": {
    "tradeId": 2,
    "matchId": 2,
    "talentId": 2,
    "buyerId": 14,
    "sellerId": 15,
    "creditPrice": 100,
    "tradeType": "PURCHASE",
    "tradeStatus": "IN_PROGRESS",
    "escrowStatus": "HELD",
    "escrowExpiresAt": "2026-06-30T12:44:06.175637",
    "createdAt": "2026-06-23T12:44:06.135002",
    "updatedAt": "2026-06-23T12:44:06.135002"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| PURCHASE 거래 생성 | 성공 | `tradeType=PURCHASE` |
| 거래 진행 상태 | 성공 | `tradeStatus=IN_PROGRESS` |
| 에스크로 보류 상태 | 성공 | `escrowStatus=HELD` |

### 3.11 결과물 업로드 URL 발급

Request:

```http
POST /api/v1/trade/2/submission/presigned-url
Authorization: Bearer {sellerAccessToken}
Content-Type: application/json
```

```json
{
  "fileName": "result.pdf"
}
```

실제 응답:

```json
{
  "success": true,
  "code": "201-4",
  "message": "Presigned URL이 발급되었습니다.",
  "data": {
    "presignedUrl": "https://team03-...amazonaws.com/trades/2/95a6641c-a1ed-4b3c-ba1e-88a12f8da9d2.pdf?...<masked>",
    "fileKey": "trades/2/95a6641c-a1ed-4b3c-ba1e-88a12f8da9d2.pdf"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| presigned URL 발급 | 성공 | URL query string은 마스킹 |
| fileKey 발급 | 성공 | 결과물 제출 요청에 사용 |

### 3.12 결과물 제출

Request:

```http
POST /api/v1/trade/2/submission
Authorization: Bearer {sellerAccessToken}
Content-Type: application/json
```

```json
{
  "fileKey": "trades/2/95a6641c-a1ed-4b3c-ba1e-88a12f8da9d2.pdf",
  "originalFileName": "result.pdf",
  "contentType": "application/pdf",
  "fileSize": 12345,
  "description": "Spring Boot 코드 리뷰 결과물입니다."
}
```

실제 응답:

```json
{
  "success": true,
  "code": "201-5",
  "message": "결과물이 제출되었습니다.",
  "data": {
    "id": 2,
    "escrowId": 2,
    "fileUrl": "https://team03-...amazonaws.com/trades/2/95a6641c-a1ed-4b3c-ba1e-88a12f8da9d2.pdf?...<masked>",
    "description": "Spring Boot 코드 리뷰 결과물입니다.",
    "submittedAt": "2026-06-23T12:45:39.6178355"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 결과물 기록 생성 | 성공 | submission `id=2` |
| escrow 연결 | 성공 | `escrowId=2` |
| 거래 상태 변경 | 성공 | 이후 재조회에서 `UNDER_REVIEW` 확인 |

### 3.13 결과물 조회

Request:

```http
GET /api/v1/trade/2/submission
Authorization: Bearer {buyerAccessToken}
```

실제 응답:

```json
{
  "success": true,
  "code": "200-9",
  "message": "결과물 조회에 성공했습니다.",
  "data": {
    "id": 2,
    "escrowId": 2,
    "fileUrl": "https://team03-...amazonaws.com/trades/2/95a6641c-a1ed-4b3c-ba1e-88a12f8da9d2.pdf?...<masked>",
    "description": "Spring Boot 코드 리뷰 결과물입니다.",
    "submittedAt": "2026-06-23T12:45:39.617836"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 구매자 결과물 조회 | 성공 | 구매자 토큰으로 조회 가능 |
| 제출 데이터 일치 | 성공 | `submission id=2`, `escrowId=2` |

### 3.14 구매 확정

Request:

```http
PATCH /api/v1/trade/2/confirm
Authorization: Bearer {buyerAccessToken}
```

실제 응답:

```json
{
  "success": true,
  "code": "200-10",
  "message": "거래가 완료되었습니다.",
  "data": {
    "tradeId": 2,
    "matchId": 2,
    "talentId": 2,
    "buyerId": 14,
    "sellerId": 15,
    "creditPrice": 100,
    "tradeType": "PURCHASE",
    "tradeStatus": "COMPLETED",
    "escrowStatus": "RELEASED",
    "escrowExpiresAt": "2026-06-30T12:44:06.175637",
    "createdAt": "2026-06-23T12:44:06.135002",
    "updatedAt": "2026-06-23T12:45:39.7084777"
  }
}
```

구매 확정 직후 거래 재조회 실제 응답:

```json
{
  "success": true,
  "code": "200-7",
  "message": "거래 조회에 성공했습니다.",
  "data": {
    "tradeId": 2,
    "tradeStatus": "UNDER_REVIEW",
    "escrowStatus": "HELD",
    "updatedAt": "2026-06-23T12:45:39.631057"
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 구매 확정 API 응답 | 성공 | 응답 DTO는 `COMPLETED/RELEASED` |
| 거래 재조회 상태 | 이슈 | 재조회 시 `UNDER_REVIEW/HELD`로 남아 있음 |
| 구매자 escrowBalance 감소 | 성공 | 구매자 잔액 조회에서 `escrowBalance=0` |
| 제공자 balance 증가 | 성공 | 제공자 잔액 `10100` |

### 3.15 잔액 확인

Request:

```http
GET /api/v1/credit/balance
Authorization: Bearer {buyerAccessToken}
```

구매자 실제 응답:

```json
{
  "success": true,
  "code": "200-0",
  "message": "요청에 성공했습니다.",
  "data": {
    "userId": 14,
    "balance": 9900,
    "escrowBalance": 0
  }
}
```

Request:

```http
GET /api/v1/credit/balance
Authorization: Bearer {sellerAccessToken}
```

제공자 실제 응답:

```json
{
  "success": true,
  "code": "200-0",
  "message": "요청에 성공했습니다.",
  "data": {
    "userId": 15,
    "balance": 10100,
    "escrowBalance": 0
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 구매자 최종 balance | 성공 | `9900` |
| 구매자 escrowBalance | 성공 | `0` |
| 제공자 최종 balance | 성공 | `10100` |
| 제공자 escrowBalance | 성공 | `0` |

### 3.16 크레딧 거래 내역 조회

Request:

```http
GET /api/v1/credit/transactions?size=20
Authorization: Bearer {buyerAccessToken}
```

구매자 실제 응답:

```json
{
  "success": true,
  "code": "200-0",
  "message": "요청에 성공했습니다.",
  "data": {
    "content": [
      {
        "transactionId": 19,
        "relatedTradeId": 2,
        "type": "ESCROW_RELEASE",
        "amount": -100,
        "balanceAfter": 9900,
        "defaultReason": "거래 완료 후 판매자 크레딧 지급",
        "detailReason": null,
        "createdAt": "2026-06-23T12:45:39.716554"
      },
      {
        "transactionId": 18,
        "relatedTradeId": 2,
        "type": "ESCROW_HOLD",
        "amount": -100,
        "balanceAfter": 9900,
        "defaultReason": "거래 완료까지 크레딧 에스크로 예치",
        "detailReason": null,
        "createdAt": "2026-06-23T12:44:06.170945"
      },
      {
        "transactionId": 16,
        "relatedTradeId": null,
        "type": "WELCOME",
        "amount": 10000,
        "balanceAfter": 10000,
        "defaultReason": "신규 가입 웰컴 크레딧 지급",
        "detailReason": null,
        "createdAt": "2026-06-23T12:44:05.382197"
      }
    ],
    "hasNext": false,
    "nextCursor": 16
  }
}
```

Request:

```http
GET /api/v1/credit/transactions?size=20
Authorization: Bearer {sellerAccessToken}
```

제공자 실제 응답:

```json
{
  "success": true,
  "code": "200-0",
  "message": "요청에 성공했습니다.",
  "data": {
    "content": [
      {
        "transactionId": 20,
        "relatedTradeId": 2,
        "type": "ESCROW_RELEASE",
        "amount": 100,
        "balanceAfter": 10100,
        "defaultReason": "거래 완료 후 판매자 크레딧 지급",
        "detailReason": null,
        "createdAt": "2026-06-23T12:45:39.719685"
      },
      {
        "transactionId": 17,
        "relatedTradeId": null,
        "type": "WELCOME",
        "amount": 10000,
        "balanceAfter": 10000,
        "defaultReason": "신규 가입 웰컴 크레딧 지급",
        "detailReason": null,
        "createdAt": "2026-06-23T12:44:05.58553"
      }
    ],
    "hasNext": false,
    "nextCursor": 17
  }
}
```

| 확인 항목 | 결과 | 비고 |
| --- | --- | --- |
| 구매자 WELCOME | 성공 | `+10000`, balanceAfter `10000` |
| 구매자 ESCROW_HOLD | 성공 | `-100`, balanceAfter `9900` |
| 구매자 ESCROW_RELEASE | 확인 필요 | `-100`으로 추가 기록됨. 실제 잔액은 `9900` 유지 |
| 제공자 WELCOME | 성공 | `+10000`, balanceAfter `10000` |
| 제공자 ESCROW_RELEASE | 성공 | `+100`, balanceAfter `10100` |

## 4. 발견 이슈

| 번호 | 우선순위 | 위치 | 실제 응답/현상 | 조치 |
| --- | --- | --- | --- | --- |
| 1 | P0 | `PATCH /api/v1/trade/{tradeId}/confirm` 이후 `GET /api/v1/trade/{tradeId}` | 확정 응답은 `COMPLETED/RELEASED`, 재조회는 `UNDER_REVIEW/HELD` | Trade/Escrow 상태 저장 또는 조회 기준 확인 필요 |
| 2 | P1 | `GET /api/v1/talents`, `GET /api/v1/talents/{talentId}` | 인증 없이 호출 시 403, 인증 포함 시 성공 | 재능 조회를 공개로 둘지 인증 필수로 둘지 정책/API 명세 싱크 필요 |
| 3 | P1 | 구매자 CreditTransaction | 구매자에게 `ESCROW_HOLD -100`과 `ESCROW_RELEASE -100`이 모두 기록됨 | 발표/문서 기준에서 구매자 차감과 에스크로 해제 내역 의미를 명확히 설명하거나 타입 분리 검토 |

## 5. MVP 완료 판단

| MVP 항목 | 판단 | 근거 |
| --- | --- | --- |
| a. 회원가입/로그인 후 초기 크레딧 지급 | 완료 | 신규 구매자 balance `10000`, WELCOME 내역 확인 |
| b. 재능 등록/조회 | 부분 완료 | 등록 성공, 인증 포함 조회 성공. 공개 조회 정책은 이슈 |
| c. 매칭 제안 생성 | 완료 | `proposalId=2`, `REQUESTED` |
| d. 매칭 제안 수락 | 완료 | `ACCEPTED` |
| e. 제안 수락 시 PURCHASE 거래 생성 | 완료 | `tradeId=2`, `tradeType=PURCHASE` |
| f. 구매자 크레딧 차감 및 에스크로 보관 | 완료 | `ESCROW_HOLD -100`, `escrowStatus=HELD` |
| g. 거래 상태 조회 | 완료 | 수락 후 `IN_PROGRESS/HELD` 조회 성공 |
| h. 거래 완료/구매 확정 처리 | 부분 완료 | 확정 응답은 성공, 재조회 상태 불일치 |
| i. 에스크로 금액 제공자 정산 | 완료 | 제공자 balance `10100`, `ESCROW_RELEASE +100` |
| j. 크레딧 변동 내역 기록 | 완료 | WELCOME, ESCROW_HOLD, ESCROW_RELEASE 조회 성공 |
| k. Swagger/Postman 순서 시연 | 부분 완료 | 위 순서로 가능하나 P0 상태 재조회 불일치 해결 필요 |
