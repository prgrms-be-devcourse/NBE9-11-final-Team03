# Baton API Specification

> 문서 버전: v1.2  
> 기준일: 2026-06-22  
> 기준 브랜치: `refactor/BATON-88-current-user`  
> 기준 PR: `#63` 참고  
> 문서 상태: 최신 구현 기준 요약  
> 관리 원칙: Swagger/OpenAPI와 실제 컨트롤러 구현을 최종 기준으로 갱신한다.

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-18 | 최초 API 명세 작성 | 작성 완료 |
| v1.1 | 2026-06-22 | 문서 버전/기준 브랜치/문서 상태 추가 | 구현 반영 필요 |
| v1.2 | 2026-06-22 | `@CurrentUser` 인증 사용자 기준, Trade/Submission/S3 API 반영 | 최신 구현 기준 요약 |

## 1. 문서 기준

- 기준 브랜치: `refactor/BATON-88-current-user`
- Swagger URL: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- 인증 사용자 식별: 컨트롤러에서 `@CurrentUser SecurityUser` 사용
- 현재 `SecurityConfig`는 auth 일부 경로를 `permitAll`로 열고, 그 외 요청도 `permitAll` 상태이나 JWT 필터와 `@CurrentUser` 기반 구조는 적용되어 있다.
- 회원가입 시 초기 크레딧 자동 지급은 아직 `AuthService.signup()`에서 연결되지 않은 P0 잔여 작업이다.

## 2. MVP API 요약

| 도메인 | 기능 | Method | Endpoint | 사용자 식별 | MVP |
| --- | --- | --- | --- | --- | --- |
| Auth | 회원가입 | POST | `/api/v1/auth/signup` | 불필요 | 포함 |
| Auth | 로그인 | POST | `/api/v1/auth/login` | 불필요 | 포함 |
| Auth | 토큰 재발급 | POST | `/api/v1/auth/reissue` | refresh cookie | 포함 |
| Auth | 로그아웃 | POST | `/api/v1/auth/logout` | `@CurrentUser` | 포함 |
| User | 회원 탈퇴 | DELETE | `/api/v1/users` | `@CurrentUser` | 포함 |
| Credit | 잔액 조회 | GET | `/api/v1/credit/balance` | `@CurrentUser` | 포함 |
| Talent | 재능 등록 | POST | `/api/v1/talents` | `@CurrentUser` | 포함 |
| Talent | 재능 수정 | PUT | `/api/v1/talents/{talentId}` | `@CurrentUser` | 포함 |
| Talent | 재능 삭제 | DELETE | `/api/v1/talents/{talentId}` | `@CurrentUser` | 포함 |
| Talent | 재능 목록 | GET | `/api/v1/talents` | 공개 | 포함 |
| Talent | 재능 검색 | GET | `/api/v1/talents/search` | 공개 | 포함 |
| Talent | 재능 상세 | GET | `/api/v1/talents/{talentId}` | 공개 | 포함 |
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

## 5. 현재 P0 잔여 확인

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| 회원가입 후 초기 크레딧 자동 지급 | 미완료 | `AuthService.signup()`에 Account 생성 TODO 존재 |
| CreditTransaction 조회 API | 미정 | 기록은 있으나 Swagger 시연용 조회 API는 별도 결정 필요 |
| PURCHASE E2E 테스트 | 보강 필요 | 회원가입 또는 계좌 준비부터 구매 확정까지 검증 필요 |

## 6. 문서화 주의

- 과거 문서의 `X-User-Id`, `userId query`, `requesterId/providerId query` 방식은 최신 컨트롤러 기준에서 제거되었다.
- 매칭 수락 시 Trade/Credit/Escrow 연결은 현재 서비스 코드에 반영되어 있다.
- 단, 신규 가입자 기준 전체 시연은 초기 크레딧 자동 지급 연결 전까지 막힐 수 있다.
