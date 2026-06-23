# Baton PURCHASE MVP 달성도 점검

> 문서 버전: v1.0  
> 기준일: 2026-06-22  
> 기준 브랜치: `refactor/BATON-88-current-user`  
> 기준 PR: PR #63 포함, `origin/dev` 최신 커밋 `ff159a7` 위에 적용한 상태  
> 문서 상태: 최신 점검 기준  
> 관리 원칙: MVP 완료 판단은 이 문서를 우선 기준으로 삼고, 코드/PR 상태 변경 시 갱신한다.

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-22 | PR #63 포함 기준 PURCHASE MVP 달성도 점검 | 최신 점검 기준 |

## 1. 현재 결론

현재 코드는 PURCHASE MVP의 핵심 거래 도메인 흐름을 상당 부분 연결했다.

다만 MVP 성공 기준인 "회원가입/로그인부터 크레딧 지급, 매칭 수락, 거래 생성, 에스크로, 구매 확정, 정산, 크레딧 이력까지 Swagger/Postman에서 순서대로 시연" 관점에서는 아직 완료가 아니다.

## 2. 달성도 요약

| 기준 | 달성도 | 판단 |
| --- | ---: | --- |
| 코드 구현 달성도 | 약 80% | 거래 생성, 에스크로 보류, 구매 확정, 정산, 이력 기록 서비스 로직은 연결됨 |
| Swagger/Postman 시연 달성도 | 약 70% | 회원가입 후 초기 크레딧 자동 지급이 끊겨 신규 사용자 기준 전체 흐름이 막힘 |
| MVP 완료 여부 | 미완료 | P0 잔여 작업 해결 전까지 최종 MVP 완료로 보기 어려움 |

## 3. MVP 성공 기준별 현재 상태

| 번호 | MVP 기준 | 현재 상태 | 근거 | 판단 |
| --- | --- | --- | --- | --- |
| 1 | 회원가입/로그인 | 구현 | `AuthController.signup`, `AuthController.login`, JWT 발급 | 완료 |
| 2 | 회원가입 후 초기 크레딧 지급 | 서비스만 구현 | `CreditService.initializeAccount()`는 있으나 `AuthService.signup()`에서 호출하지 않음 | P0 미완료 |
| 3 | 재능 등록/조회 | 구현 | `TalentController` 등록, 목록, 검색, 상세 조회 | 완료 |
| 4 | 매칭 제안 생성 | 구현 | `MatchProposalController.createMatchProposal()` | 완료 |
| 5 | 매칭 제안 수락 | 구현 | `MatchProposalService.acceptMatchProposal()` | 완료 |
| 6 | PURCHASE 거래 생성 | 구현 | 매칭 수락 시 `TradeService.create()` 호출 | 완료 |
| 7 | 구매자 크레딧 차감 | 구현, 단 계좌 필요 | `CreditService.holdForEscrow()`에서 balance 차감 및 escrowBalance 증가 | 부분 완료 |
| 8 | 에스크로 보관 | 구현 | `EscrowService.create()`로 `HELD` 에스크로 생성 | 완료 |
| 9 | 거래 상태 조회 | 구현 | `TradeController.getTrade()` | 완료 |
| 10 | 거래 완료/구매 확정 | 구현 | `TradeSubmissionService.confirmPurchase()` | 완료 |
| 11 | 제공자 크레딧 정산 | 구현 | `CreditService.settleEscrow()`에서 구매자 escrowBalance 차감, 판매자 balance 증가 | 완료 |
| 12 | CreditTransaction 변동 내역 기록 | 기록 구현, 조회 API 없음 | `WELCOME`, `ESCROW_HOLD`, `ESCROW_RELEASE`, `REFUND` 기록 로직 존재 | 부분 완료 |

## 4. PURCHASE MVP 실제 흐름 연결 상태

현재 구현상 정상 PURCHASE 거래는 아래처럼 흐른다.

```text
매칭 제안 생성
-> 제공자 제안 수락
-> Trade 생성
-> 구매자 balance 차감
-> 구매자 escrowBalance 증가
-> CreditTransaction ESCROW_HOLD 기록
-> Escrow HELD 생성
-> 판매자 결과물 제출
-> Trade UNDER_REVIEW
-> 구매자 구매 확정
-> Escrow RELEASED
-> Trade COMPLETED
-> 구매자 escrowBalance 차감
-> 판매자 balance 증가
-> CreditTransaction ESCROW_RELEASE 2건 기록
```

이 흐름 자체는 서비스 코드 기준으로 연결되어 있다.

## 5. P0 잔여 작업

| 우선순위 | 작업 | 이유 | 완료 기준 |
| --- | --- | --- | --- |
| P0 | 회원가입 성공 후 `CreditService.initializeAccount(userId)` 자동 호출 | 신규 사용자가 가입 직후 크레딧 계좌가 없어 PURCHASE 흐름을 시작할 수 없음 | 회원가입 직후 `/api/v1/credit/balance`에서 초기 balance 확인 |
| P0 | CreditTransaction 조회 API 추가 여부 결정 | MVP 성공 기준에 "변동 내역 기록"이 포함되어 있고, Swagger/Postman 시연에서는 API 조회 수단이 필요할 수 있음 | `/api/v1/credit/transactions` 형태의 본인 이력 조회 API 또는 DB 확인 방식 확정 |
| P0 | PURCHASE E2E 통합 테스트 추가 | 현재는 각 조각 테스트 중심이라 전체 흐름 회귀를 한 번에 막기 어려움 | signup 또는 계좌 준비부터 confirmPurchase 이후 정산까지 검증 |

## 6. P1 보강 작업

| 우선순위 | 작업 | 이유 |
| --- | --- | --- |
| P1 | `Idempotency-Key` 원본 blank 검증 | 현재 서버 prefix를 붙이면 원본 header가 blank여도 최종 key가 blank가 아니게 될 수 있음 |
| P1 | 무료 재능 가격 정책 확정 | `holdForEscrow()`는 amount <= 0을 거부하므로 0 크레딧 재능을 허용하면 수락 흐름이 실패할 수 있음 |
| P1 | Swagger 설명 인코딩 정리 | 일부 한글 설명이 깨져 보여 발표/시연 문서 품질에 영향 |
| P1 | AuthControllerTest 쿠키 `Secure` 기대값 이슈 분리 | 전체 테스트 실행 시 기존 쿠키 테스트 2건이 실패할 수 있음 |

## 7. P2 후순위

| 우선순위 | 작업 | 이유 |
| --- | --- | --- |
| P2 | SWAP 거래 확장 | PURCHASE MVP 완료 후 양방향 거래 그룹 구조로 확장 |
| P2 | 리뷰/신뢰 점수 | 거래 완료 이후 신뢰도 강화 기능 |
| P2 | 관리자 기능 | 운영 안정성 보강 |
| P2 | 채팅 고도화 | 현재 기본 REST API는 있으나 MVP 핵심 거래 흐름과 분리 가능 |

## 8. 팀장 관점 확인 사항

팀장이 바로 확인해야 할 것:

- 회원가입 시 초기 크레딧 자동 지급을 이번 MVP P0로 바로 처리할지 결정
- CreditTransaction 조회 API를 MVP 시연에 포함할지 결정
- PR #63 CI와 리뷰 상태 확인
- Swagger/Postman 시연 순서에서 사용할 테스트 사용자/데이터 준비 방식 결정

팀원에게 맡길 것:

- 초기 크레딧 자동 지급 연결 및 테스트
- CreditTransaction 조회 API 또는 시연용 확인 방식 구현
- PURCHASE E2E 통합 테스트 작성
- Swagger 설명 인코딩 정리

다른 채팅으로 넘길 것:

- 최종 발표 문서/시연 대본 업데이트
- Postman 컬렉션 또는 Swagger 시연 순서 정리
- SWAP, 리뷰, 관리자 기능 설계

## 9. 최종 판단

PR #63까지 포함하면 PURCHASE 거래의 핵심 도메인 연결은 거의 완성 단계다.

하지만 "신규 회원가입부터 시작하는 MVP 시연" 기준에서는 초기 크레딧 자동 지급이 연결되지 않아 아직 P0 미완료다. 이 작업을 해결하고, 크레딧 변동 내역 확인 방식을 정하면 MVP 달성도는 약 90% 이상으로 올라갈 수 있다.
