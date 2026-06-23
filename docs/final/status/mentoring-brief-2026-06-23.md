# Baton 멘토링 사전 브리프

> 기준일: 2026-06-23
> 목적: 멘토링 전에 현재 상태, 오늘 볼 코드 흐름, 참고 문서를 간단히 공유한다.

## 1. 현재 상태

Baton은 크레딧 기반 재능 거래/교환 서비스다.

현재 PURCHASE MVP는 회원가입부터 재능 등록, 매칭 제안, 제안 수락, 거래 생성, 크레딧 에스크로 보류, 결과물 제출, 구매 확정, 제공자 정산, 크레딧 이력 기록까지 정상 흐름을 검증한 상태다.

채팅은 REST API와 WebSocket/STOMP 기반 송수신 컨트롤러가 들어온 상태다. SWAP, 관리자 기능, 리뷰/신뢰 점수는 PURCHASE 이후 P1 범위로 보고 있다.

## 2. 멘토링 진행 흐름

기본 진행은 PURCHASE MVP 핵심 흐름을 먼저 검토받는 방식으로 잡는다.
이후에는 아래 질문에 한정하지 않고, 멘토님이 보시기에 구조상 위험하거나 개선 여지가 큰 부분을 추가로 리뷰받고 싶다.

| 순서 | 내용 | 보여드릴 자료 |
|---|---|---|
| 1 | 프로젝트 현재 상태 간단 공유 | [프로젝트 총괄 문서](../overview/project-master.md) |
| 2 | PURCHASE MVP 달성 상태 확인 | [MVP 달성도 점검](mvp-achievement-status-2026-06-22.md) |
| 3 | PURCHASE 핵심 흐름 코드 리뷰 | [MatchProposal 수락 흐름](../architecture/match-proposal-accept-flow.md) |
| 4 | 실제 API 테스트 결과 확인 | [MVP PURCHASE 테스트 기록](../../testing/mvp-purchase-flow-test-2026-06-23.md) |
| 5 | 테스트 작성/검증 방향 확인 | [MVP PURCHASE 테스트 기록](../../testing/mvp-purchase-flow-test-2026-06-23.md) |
| 6 | 멘토님 판단으로 추가 개선 포인트 리뷰 | 코드 직접 확인 |
| 7 | 확장 질문 확인 | 이 문서의 6번 섹션 |

## 3. 먼저 리뷰받고 싶은 코드 흐름

```text
Auth.signup
-> CreditAccount 생성 / WELCOME 이력 기록
-> Talent 등록/조회
-> MatchProposal 생성
-> MatchProposal 수락
-> Trade 생성
-> 구매자 Credit hold
-> Escrow HELD 생성
-> 결과물 제출
-> 구매 확정
-> Escrow RELEASED
-> Trade COMPLETED
-> 제공자 정산
-> CreditTransaction 기록
```

가장 먼저 보고 싶은 부분은 다음 두 흐름이다.

- `MatchProposal 수락 -> Trade/Credit/Escrow 연결`
- `구매 확정 -> Escrow release/Credit 정산`

## 4. 메인 질문

### PURCHASE MVP 코드 리뷰

현재 정상 흐름은 동작한다.
다만 거래/크레딧/에스크로가 함께 움직이므로 트랜잭션 경계, 상태 전이, 동시성 방어가 적절한지 확인받고 싶다.

먼저 보고 싶은 코드:

| 코드 | 확인하고 싶은 지점 |
|---|---|
| [MatchProposalService](../../../src/main/java/com/back/baton/domain/matching/service/MatchProposalService.java) | 제안 수락 시 Trade 생성, Credit hold, Escrow 생성이 한 트랜잭션 안에서 안전하게 연결되는지 |
| [TradeSubmissionService](../../../src/main/java/com/back/baton/domain/trade/service/TradeSubmissionService.java) | 결과물 제출 후 구매 확정, Trade 완료, Escrow release, Credit 정산 흐름이 적절한지 |
| [TradeService](../../../src/main/java/com/back/baton/domain/trade/service/TradeService.java) | 거래 생성/조회/취소 책임과 상태 변경 검증이 적절한지 |
| [CreditService](../../../src/main/java/com/back/baton/domain/credit/service/CreditService.java) | 잔액 차감, 에스크로 보류, 환불, 정산, CreditTransaction 기록 책임이 적절한지 |
| [EscrowService](../../../src/main/java/com/back/baton/domain/escrow/service/EscrowService.java) | Escrow 생성, release, refund 상태 전이와 Trade/Credit과의 책임 경계가 적절한지 |

확인받고 싶은 부분:

- MatchProposal, Trade, Credit, Escrow 책임 분리가 적절한지
- 중복 수락, 중복 구매 확정, 재시도 상황에 대한 방어가 충분한지
- 락, 조건부 update, 멱등키를 더 보강해야 할 지점이 있는지
- 실패 케이스 테스트를 어디까지 잡는 게 좋을지
- JPA 벌크 업데이트와 영속성 컨텍스트 clear/flush 타이밍 때문에 Trade/Escrow 상태 저장이 누락될 위험은 없는지

특히 걱정되는 지점:

- 같은 매칭 제안을 여러 번 수락하거나, 수락/거절이 거의 동시에 들어왔을 때 Trade/Escrow/Credit이 중복 생성되지 않는지
- 구매 확정 API가 중복 호출될 때 제공자에게 중복 정산되지 않는지
- Credit 정산 중 예외가 발생했을 때 Trade/Escrow 상태와 CreditTransaction 이력이 불일치하지 않는지
- Idempotency-Key를 현재 수준으로 둬도 되는지, 요청 내용까지 비교하는 방식이 필요한지

위 흐름을 먼저 검토받은 뒤, 멘토님 판단으로 추가 리뷰가 필요해 보이는 도메인, 테스트, 설계, 코드 스타일이 있다면 자유롭게 짚어주시면 좋겠다.

## 5. 테스트 코드 작성/검증 현황

현재 PURCHASE MVP는 수동 API 테스트로 정상 흐름을 검증했다.

현재 확보한 검증:

- 회원가입부터 구매 확정, 정산, CreditTransaction 조회까지 Swagger/Postman 기준 정상 흐름 재현
- 구매 확정 후 Trade/Escrow 상태 재조회 이슈를 문서화
- 실패 케이스와 동시성 케이스는 자동 테스트 보강 필요

멘토님께 확인받고 싶은 점:

- PURCHASE 전체 E2E 테스트를 어느 수준까지 자동화하는 게 좋은지
- 중복 수락, 중복 구매 확정, 권한 없음, 크레딧 부족 같은 실패 케이스 중 무엇을 우선 작성해야 하는지
- 동시성 테스트를 실제 멀티스레드로 작성할지, 상태 조건 검증 중심으로 작성할지
- 구매 확정 후 Trade/Escrow 상태 저장 이슈는 어떤 방식의 통합 테스트로 고정하는 것이 좋은지

## 6. 확장 질문

PURCHASE MVP 이후 P1 범위로 SWAP, 채팅, 리뷰/신뢰 점수, 관리자 기능을 보고 있다.
멘토링에서는 각 기능의 세부 구현을 모두 리뷰받기보다, 남은 기간 기준으로 어디까지 구현하면 제출 완성도가 있는지 확인받고 싶다.

### SWAP 설계

SWAP을 단방향 Trade 2개와 하나의 거래 그룹으로 묶는 방식을 고려하고 있다.

```text
SwapGroup
├─ Trade A -> B
└─ Trade B -> A
```

확인받고 싶은 점:

- 이 모델링이 적절한지
- 멱등키를 SWAP 요청 전체에 둘지, 방향별 Trade마다 둘지
- 한쪽 거래만 성공하는 상황을 어떻게 막거나 복구하는 게 좋을지

### 채팅 기능 검증/고도화

채팅은 REST 채팅방/메시지 API와 WebSocket/STOMP 기반 송수신 컨트롤러가 들어온 상태다.

확인받고 싶은 점:

- 현재 구조가 제출/시연 가능한 최소 수준인지
- 채팅방을 매칭 단위로 만들지, 거래 단위로 만들지
- Redis Pub/Sub까지 지금 붙이는 것이 필요한지
- 메시지 읽음 처리, 권한 검증, 메시지 보존 정책 중 무엇을 우선해야 하는지

### 리뷰/신뢰 점수

리뷰/신뢰 점수는 거래 완료 이후 사용자 신뢰를 쌓기 위한 기능으로 보고 있다.

확인받고 싶은 점:

- 리뷰는 완료된 Trade에 대해 1회만 작성 가능하게 하는 것이 적절한지
- 리뷰 점수를 User의 trustScore에 즉시 반영할지, 별도 집계로 둘지
- 악성/허위 리뷰 대응은 지금 구현할지, 정책 문서로 분리해도 되는지
- 제출 전 최소 구현 기준을 리뷰 작성/조회까지로 봐도 되는지

### 관리자 기능

관리자 기능은 운영 안정성과 제출 완성도를 위해 P1 범위로 보고 있다.

확인받고 싶은 점:

- 제출 전 최소 관리자 기능을 회원/거래/재능 조회 정도로 봐도 되는지
- 상태 변경 기능까지 넣는 것이 좋은지
- 관리자 권한 검증과 감사 로그 중 무엇을 우선해야 하는지
- 분쟁 중재 기능은 지금 구현할지, 정책/확장 계획으로 분리해도 되는지

### 로깅/모니터링

최종 제출 항목에 모니터링/부하테스트 결과가 있어 최소 증빙이 필요하다.

확인받고 싶은 점:

- Actuator + 주요 API 로그 + 간단한 부하테스트 정도면 충분한지
- Prometheus/Grafana까지 붙이는 것이 필요한지
- 거래, 에스크로, 정산 흐름에서 꼭 남겨야 할 로그는 무엇인지

### 공통 응답 코드

SuccessCode/ErrorCode가 커지고 있어 도메인별 코드 번호를 나누는 방식을 검토 중이다.

확인받고 싶은 점:

- 성공/에러 코드를 도메인별 번호 대역으로 나누는 방식이 적절한지
- 너무 세분화하지 않으려면 어떤 기준으로 나누는 게 좋은지
- 프론트 연동과 API 문서 관점에서 어떤 수준이 적절한지

## 7. 상황별로 보여드릴 문서

| 상황 | 문서 |
|---|---|
| 프로젝트 전체 방향을 설명할 때 | [프로젝트 총괄 문서](../overview/project-master.md) |
| MVP가 어디까지 됐는지 보여줄 때 | [MVP 달성도 점검](mvp-achievement-status-2026-06-22.md) |
| 매칭 수락 후 거래/크레딧/에스크로 연결을 설명할 때 | [MatchProposal 수락 흐름](../architecture/match-proposal-accept-flow.md) |
| 실제 Swagger/Postman 테스트 결과를 보여줄 때 | [MVP PURCHASE 테스트 기록](../../testing/mvp-purchase-flow-test-2026-06-23.md) |
| 테스트 보강 방향을 설명할 때 | [MVP PURCHASE 테스트 기록](../../testing/mvp-purchase-flow-test-2026-06-23.md) |
| API 목록과 인증 기준을 확인할 때 | [API 명세서](../../api/api-spec-v1.md) |
| PURCHASE 이후 P1 범위를 설명할 때 | [MVP 이후 필수 구현 범위 메모](post-mvp-required-scope-note.md) |
| 채팅 구현 상태를 확인할 때 | [ChatRoomController](../../../src/main/java/com/back/baton/domain/chat/controller/ChatRoomController.java), [ChatMessageController](../../../src/main/java/com/back/baton/domain/chat/controller/ChatMessageController.java), [ChatWebSocketController](../../../src/main/java/com/back/baton/domain/chat/controller/ChatWebSocketController.java), [ChatService](../../../src/main/java/com/back/baton/domain/chat/service/ChatService.java) |
| 리뷰/관리자 최소 범위를 논의할 때 | [최종 제출물 갭 체크리스트](final-submission-gap-checklist.md) |

## 8. 멘토링 후 얻고 싶은 결론

- PURCHASE MVP 구조가 제출 가능한 수준인지
- 남은 기간 안에 꼭 고쳐야 할 위험 지점이 있는지
- 실패 케이스 테스트와 동시성 보강 우선순위
- SWAP, 채팅, 리뷰/신뢰 점수, 관리자 기능의 최소 완성 기준
- 로깅/모니터링, 응답 코드 정리 방향
