# Baton 최종 API 명세

> 문서 버전: v2.0
> 기준일: 2026-07-01
> 최종 기준: 실제 Controller와 Swagger/OpenAPI
> 최신 DEV: `bc39d192` (CI 성공)
> 실제 배포 main: `4f18382` (CI/CD 성공)

## 1. 접속 정보

- Local Swagger: `http://localhost:8080/swagger-ui/index.html`
- Local OpenAPI: `http://localhost:8080/v3/api-docs`
- Deploy Swagger: `http://54.116.23.255/swagger-ui/index.html`
- Deploy API: `http://54.116.23.255`
- 인증: `Authorization: Bearer {accessToken}`
- 사용자 식별: 인증 이후 `@CurrentUser SecurityUser`

## 2. PURCHASE MVP API

| 도메인 | 기능 | Method | Endpoint | 상태 |
| --- | --- | --- | --- | --- |
| Auth | 회원가입 | POST | `/api/v1/auth/signup` | 구현 완료 |
| Auth | 로그인 | POST | `/api/v1/auth/login` | 구현 완료 |
| Auth | 토큰 재발급 | POST | `/api/v1/auth/reissue` | 구현 완료 |
| Auth | 로그아웃 | POST | `/api/v1/auth/logout` | 구현 완료 |
| User | 회원 탈퇴 | DELETE | `/api/v1/users` | 구현 완료 |
| Credit | 잔액 조회 | GET | `/api/v1/credit/balance` | 구현 완료 |
| Credit | 거래 내역 조회 | GET | `/api/v1/credit/transactions` | 구현 완료 |
| Talent | 등록 | POST | `/api/v1/talents` | 구현 완료 |
| Talent | 목록 | GET | `/api/v1/talents` | 구현 완료, 비로그인 허용 |
| Talent | 검색 | GET | `/api/v1/talents/search` | 구현 완료, 비로그인 허용 |
| Talent | 상세 | GET | `/api/v1/talents/{talentId}` | 구현 완료, 비로그인 허용 |
| Talent | 수정 | PUT | `/api/v1/talents/{talentId}` | 구현 완료 |
| Talent | 삭제 | DELETE | `/api/v1/talents/{talentId}` | 구현 완료 |
| Matching | 제안 생성 | POST | `/api/v1/match-proposals` | 구현 완료 |
| Matching | 제안 수락 | PATCH | `/api/v1/match-proposals/{proposalId}/accept` | 구현 완료 |
| Matching | 제안 거절 | PATCH | `/api/v1/match-proposals/{proposalId}/reject` | 구현 완료 |
| Trade | 상세 조회 | GET | `/api/v1/trade/{tradeId}` | 구현 완료 |
| Trade | 취소 | PATCH | `/api/v1/trade/{tradeId}/cancel` | 구현 완료 |
| Trade | 결과물 URL 발급 | POST | `/api/v1/trade/{tradeId}/submission/presigned-url` | 구현, 시연 검증 필요 |
| Trade | 결과물 제출 | POST | `/api/v1/trade/{tradeId}/submission` | 구현 완료 |
| Trade | 결과물 조회 | GET | `/api/v1/trade/{tradeId}/submission` | 구현 완료 |
| Trade | 구매 확정 | PATCH | `/api/v1/trade/{tradeId}/confirm` | 구현 완료 |

제안 수락 API에는 재호출 방지를 위해 `Idempotency-Key` 헤더가 필요하다.

재능 목록/검색 응답은 최신 DEV에서 `authorId`, `authorNickname`을 포함한다. 목록/검색 조회는 상세 조회와 분리되어 조회수를 증가시키지 않으며, `ACTIVE` 재능만 반환한다.

최신 DEV에서 이미 `IN_PROGRESS`인 거래는 취소할 수 없다. 취소 요청 시 `TRADE-400-009`와 "이미 진행된 거래는 취소할 수 없습니다." 메시지를 반환한다.

## 3. 보조 및 확장 API

| 영역 | API | 상태 | 발표 기준 |
| --- | --- | --- | --- |
| Matching | `/api/v1/match-recommendations` | 구현 완료 | 조건 기반 추천으로 설명 |
| Talent Attachment | `/api/v1/talents/{talentId}/attachments/**` | 구현, 배포 검증 필요 | 검증된 범위만 사용 |
| Chat REST | `/api/v1/chat-rooms/**` | 구현, 시연 검증 필요 | 보조 시연 후보 |
| Chat STOMP | `/app/chat-rooms/{chatRoomId}/messages`, `/read` | 구현, 시연 검증 필요 | 연결 검증 후 사용 |
| SWAP | 관련 API | 최종 구현 범위 확인 필요 | 근거 없으면 확장 계획 |
| Admin/Review | 관련 API | 최종 구현 범위 확인 필요 | 근거 없으면 확장 계획 |

## 4. 상태 전이

```text
MatchProposal: REQUESTED -> ACCEPTED | REJECTED
Trade: IN_PROGRESS -> UNDER_REVIEW -> COMPLETED
Trade cancel: IN_PROGRESS 상태는 취소 불가
Escrow: HELD -> RELEASED | REFUNDED
```

매칭 수락 시 PURCHASE Trade, Credit hold, Escrow HELD가 하나의 트랜잭션 흐름에서 생성된다. 구매 확정 시 Trade COMPLETED, Escrow RELEASED, 제공자 정산, CreditTransaction 기록이 이어진다.

## 5. 최종 검증 상태

| 검증 | 결과 |
| --- | --- |
| 회원가입 후 초기 크레딧 | 10,000 지급 및 WELCOME 기록 확인 |
| PURCHASE 정상 흐름 | 로컬 실제 HTTP 호출 통과 |
| 구매 확정 DB 재조회 | COMPLETED/RELEASED 확인 |
| 배포 purchase smoke | k6 12요청, 실패율 0%, p95 220.89ms |
| 배포 Swagger/OpenAPI | 2026-07-01 HTTP 200 확인 |
| 최신 DEV CI | `bc39d192`, 성공 |
| MatchProposal 정렬 | DEV에서 `createdAt desc, id desc` 보조 정렬 반영 |

## 6. 시연 주의

- 카테고리와 재능 GET API는 최신 DEV Security 설정에서 비로그인 접근을 허용한다.
- 매칭 수락 후 `tradeId` 확보 방법을 녹화 전에 확정한다.
- API 문서보다 실제 배포 Swagger와 Controller가 우선한다.
- 완료 근거가 없는 SWAP/Admin/Review 기능은 완성 기능으로 발표하지 않는다.
- 실제 배포 main은 `4f18382`이고 최신 DEV는 PR #122 병합 전이므로, DEV 전용 응답 필드와 CD 보완을 배포 완료로 설명하지 않는다.
