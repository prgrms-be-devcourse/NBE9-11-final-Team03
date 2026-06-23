# Baton API Specification

> 문서 버전: v1.3
> 기준일: 2026-06-23
> 기준 브랜치: `dev`
> 기준 PR: `#62`, `#63`, `#64`, `#67`, `#68` 반영 기준
> 문서 상태: MVP 수동 API 테스트 결과 반영
> 관리 원칙: Swagger/OpenAPI와 실제 컨트롤러 구현을 최종 기준으로 갱신한다.

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-18 | 최초 API 명세 작성 | 작성 완료 |
| v1.1 | 2026-06-22 | 문서 버전/기준 브랜치/문서 상태 추가 | 구현 반영 필요 |
| v1.2 | 2026-06-22 | `@CurrentUser` 인증 사용자 기준, Trade/Submission/S3 API 반영 | 최신 구현 기준 요약 |
| v1.3 | 2026-06-23 | 회원가입 초기 크레딧, CreditTransaction 조회, Security 인증 정책, MVP 수동 API 테스트 결과 반영 | 최신 테스트 기준 |

## 1. 문서 기준

- 기준 브랜치: `dev`
- Swagger URL: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 인증 사용자 식별: 컨트롤러에서 `@CurrentUser SecurityUser` 사용
- 현재 `SecurityConfig`는 회원가입/로그인/토큰 재발급, Swagger, websocket 경로만 `permitAll`이고 그 외 API는 인증이 필요하다.
- 회원가입 시 `AuthService.signup()`에서 `CreditService.initializeAccount(userId)`를 호출해 초기 크레딧 계좌와 WELCOME 이력을 생성한다.

## 2. MVP API 요약

| 도메인 | 기능 | Method | Endpoint | 사용자 식별 | MVP |
| --- | --- | --- | --- | --- | --- |
| Auth | 회원가입 | POST | `/api/v1/auth/signup` | 불필요 | 포함 |
| Auth | 로그인 | POST | `/api/v1/auth/login` | 불필요 | 포함 |
| Auth | 토큰 재발급 | POST | `/api/v1/auth/reissue` | refresh cookie | 포함 |
| Auth | 로그아웃 | POST | `/api/v1/auth/logout` | `@CurrentUser` | 포함 |
| User | 회원 탈퇴 | DELETE | `/api/v1/users` | `@CurrentUser` | 포함 |
| Credit | 잔액 조회 | GET | `/api/v1/credit/balance` | `@CurrentUser` | 포함 |
| Credit | 크레딧 거래 내역 조회 | GET | `/api/v1/credit/transactions` | `@CurrentUser` | 포함 |
| Talent | 재능 등록 | POST | `/api/v1/talents` | `@CurrentUser` | 포함 |
| Talent | 재능 수정 | PUT | `/api/v1/talents/{talentId}` | `@CurrentUser` | 포함 |
| Talent | 재능 삭제 | DELETE | `/api/v1/talents/{talentId}` | `@CurrentUser` | 포함 |
| Talent | 재능 목록 | GET | `/api/v1/talents` | Bearer Token 필요 | 포함 |
| Talent | 재능 검색 | GET | `/api/v1/talents/search` | Bearer Token 필요 | 포함 |
| Talent | 재능 상세 | GET | `/api/v1/talents/{talentId}` | Bearer Token 필요 | 포함 |
| Matching | 추천 목록 | GET | `/api/v1/match-recommendations?talentId={talentId}` | `@CurrentUser` | 보조 |
| Matching | 추천 상세 | GET | `/api/v1/match-recommendations/{providerTalentId}?requesterTalentId={requesterTalentId}` | `@CurrentUser` | 보조 |
| MatchProposal | 제안 생성 | POST | `/api/v1/match-proposals` | `@CurrentUser` | 포함 |
| MatchProposal | 제안 수락 | PATCH | `/api/v1/match-proposals/{proposalId}/accept` | `@CurrentUser`, `Idempotency-Key` | 포함 |
| MatchProposal | 제안 거절 | PATCH | `/api/v1/match-proposals/{proposalId}/reject` | `@CurrentUser` | 포함 |
| Trade | 거래 조회 | GET | `/api/v1/trade/{tradeId}` | `@CurrentUser` | 포함 |
| Trade | 거래 취소 | PATCH | `/api/v1/trade/{tradeId}/cancel` | `@CurrentUser` | 포함 |
| Trade | 결과물 업로드 URL | POST | `/api/v1/trade/{tradeId}/submission/presigned-url` | `@CurrentUser` | 포함 |
| Trade | 결과물 제출 | POST | `/api/v1/trade/{tradeId}/submission` | `@CurrentUser` | 포함 |
| Trade | 결과물 조회 | GET | `/api/v1/trade/{tradeId}/submission` | `@CurrentUser` | 포함 |
| Trade | 구매 확정 | PATCH | `/api/v1/trade/{tradeId}/confirm` | `@CurrentUser` | 포함 |

## 3. 고도화/보조 API

| 도메인 | 기능 | Method | Endpoint | 상태 |
| --- | --- | --- | --- | --- |
| Talent Attachment | presigned URL 발급 | POST | `/api/v1/talents/{talentId}/attachments/presigned-url` | S3 고도화 |
| Talent Attachment | 첨부 저장 | POST | `/api/v1/talents/{talentId}/attachments` | S3 고도화 |
| Talent Attachment | 첨부 목록 | GET | `/api/v1/talents/{talentId}/attachments` | S3 고도화 |
| Talent Attachment | 첨부 삭제 | DELETE | `/api/v1/talents/{talentId}/attachments/{attachmentId}` | S3 고도화 |
| ChatRoom | 채팅방 생성/조회 | POST | `/api/v1/chat-rooms` | 고도화 |
| ChatMessage | 메시지 전송 | POST | `/api/v1/chat-rooms/{chatRoomId}/messages` | 고도화 |
| ChatMessage | 메시지 목록 | GET | `/api/v1/chat-rooms/{chatRoomId}/messages` | 고도화 |

## 4. PURCHASE MVP 시연 순서

1. 구매자/제공자 회원가입
2. 로그인 후 access token 확보
3. 초기 크레딧 잔액 확인
4. 제공자 재능 등록
5. 구매자 재능 조회
6. 구매자 매칭 제안 생성
7. 제공자 매칭 제안 수락
8. 수락 처리 중 Trade 생성, Credit hold, Escrow HELD 생성
9. 거래 조회
10. 제공자 결과물 제출
11. 구매자 결과물 조회
12. 구매자 구매 확정
13. Escrow RELEASED, Trade COMPLETED, 제공자 정산 확인

## 5. 현재 P0/P1 잔여 확인

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| 회원가입 후 초기 크레딧 자동 지급 | 구현/검증 완료 | `AuthService.signup()`에서 `CreditService.initializeAccount(userId)` 호출, 신규 사용자 balance `10000` 확인 |
| CreditTransaction 조회 API | 구현 | `GET /api/v1/credit/transactions`로 본인 거래 내역 조회 가능 |
| PURCHASE 정상 흐름 수동 API 테스트 | 완료 | 회원가입부터 구매 확정/정산/거래 내역 조회까지 localhost 기준 재현 |
| 구매 확정 후 거래 재조회 상태 | P0 확인 필요 | 확정 응답은 `COMPLETED/RELEASED`, 직후 상세 재조회는 `UNDER_REVIEW/HELD`로 관측됨 |
| 재능 조회 인증 정책 | P1 확인 필요 | 컨트롤러는 사용자 식별이 없지만 Security 정책상 Bearer Token 필요 |

## 6. 문서화 주의

- 과거 문서의 `X-User-Id`, `userId query`, `requesterId/providerId query` 방식은 최신 컨트롤러 기준에서 제거되었다.
- 매칭 수락 시 Trade/Credit/Escrow 연결은 현재 서비스 코드에 반영되어 있다.
- 신규 가입자 기준 전체 시연은 가능하다. 단, 구매 확정 직후 거래 상세 재조회 상태 불일치는 수정 또는 발표 전 재검증이 필요하다.
- 재능 목록/검색/상세 조회는 Swagger 시연 시 Bearer Token을 포함한다.
