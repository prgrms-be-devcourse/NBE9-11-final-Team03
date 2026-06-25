# Baton PURCHASE MVP 달성도 점검

> 문서 버전: v1.1
> 기준일: 2026-06-23
> 기준 브랜치: `dev`
> 기준 PR: `#62`, `#63`, `#64`, `#67`, `#68` 반영 기준
> 문서 상태: 최신 테스트 결과와 MVP 이후 필수 구현 범위 반영
> 관리 원칙: MVP 완료 판단은 이 문서를 우선 기준으로 삼고, 코드/PR 상태 변경 시 갱신한다.

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-22 | PR #63 포함 기준 PURCHASE MVP 달성도 점검 | 최신 점검 기준 |
| v1.1 | 2026-06-23 | 회원가입 초기 크레딧, CreditTransaction 조회, MVP 수동 API 테스트 결과와 재조회 상태 이슈 반영 | 최신 테스트 기준 |
| v1.2 | 2026-06-23 | SWAP/관리자/리뷰/채팅을 P1 필수 구현/고도화 범위로 재분류 | 최신 총괄 기준 |

## 1. 현재 결론

현재 코드는 PURCHASE MVP 정상 흐름을 회원가입부터 정산/크레딧 이력 조회까지 API 기준으로 재현할 수 있다.

2026-06-23 수동 테스트에서 구매 확정 응답은 `COMPLETED/RELEASED`였지만 직후 거래 상세 재조회가 `UNDER_REVIEW/HELD`로 남는 상태 저장 이슈가 확인되었다. 이후 `CreditAccountRepository` 벌크 업데이트에 `flushAutomatically = true`를 추가했고, 통합 테스트에서 구매 확정 후 DB 재조회가 `COMPLETED/RELEASED`로 유지됨을 확인했다. 발표 전에는 실행 서버를 재기동한 뒤 Swagger/Postman으로 같은 흐름을 한 번 더 재검증한다.

## 2. 달성도 요약

| 기준 | 달성도 | 판단 |
| --- | ---: | --- |
| 코드 구현 달성도 | 약 98% | 회원가입 초기 크레딧, 거래 생성, 에스크로 보류, 제출, 구매 확정, 정산, 이력 조회까지 구현됨. 상태 저장 이슈는 통합 테스트로 해결 확인 |
| Swagger/Postman 시연 달성도 | 약 98% | 정상 흐름 재현 가능. 구매 확정 후 상세 재조회 API 재검증 완료 |
| MVP 완료 여부 | P0 정상 흐름 완료 | P0 저장 이슈는 통합 테스트와 실행 서버 API 재검증으로 해결 확인. 남은 것은 실패 케이스 보강 |

## 3. MVP 성공 기준별 현재 상태

| 번호 | MVP 기준 | 현재 상태 | 근거 | 판단 |
| --- | --- | --- | --- | --- |
| 1 | 회원가입/로그인 | 구현 | `AuthController.signup`, `AuthController.login`, JWT 발급 | 완료 |
| 2 | 회원가입 후 초기 크레딧 지급 | 구현/검증 완료 | `AuthService.signup()`에서 `CreditService.initializeAccount()` 호출, 신규 사용자 balance `10000` 확인 | 완료 |
| 3 | 재능 등록/조회 | 구현 | `TalentController` 등록, 목록, 검색, 상세 조회 | 완료 |
| 4 | 매칭 제안 생성 | 구현 | `MatchProposalController.createMatchProposal()` | 완료 |
| 5 | 매칭 제안 수락 | 구현 | `MatchProposalService.acceptMatchProposal()` | 완료 |
| 6 | PURCHASE 거래 생성 | 구현 | 매칭 수락 시 `TradeService.create()` 호출 | 완료 |
| 7 | 구매자 크레딧 차감 | 구현/검증 완료 | `CreditService.holdForEscrow()`에서 balance 차감 및 escrowBalance 증가, 실제 응답 확인 | 완료 |
| 8 | 에스크로 보관 | 구현 | `EscrowService.create()`로 `HELD` 에스크로 생성 | 완료 |
| 9 | 거래 상태 조회 | 구현 | `TradeController.getTrade()` | 완료 |
| 10 | 거래 완료/구매 확정 | 코드 수정/통합 테스트/API 재검증 완료 | confirm 후 DB 재조회 및 서버 API 재조회 모두 `Trade.status=COMPLETED`, `Escrow.status=RELEASED` 확인 | 완료 |
| 11 | 제공자 크레딧 정산 | 구현 | `CreditService.settleEscrow()`에서 구매자 escrowBalance 차감, 판매자 balance 증가 | 완료 |
| 12 | CreditTransaction 변동 내역 기록 | 구현/검증 완료 | `GET /api/v1/credit/transactions`로 WELCOME, ESCROW_HOLD, ESCROW_RELEASE 조회 | 완료 |

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

## 5. P0/P1 잔여 작업

| 우선순위 | 작업 | 이유 | 완료 기준 |
| --- | --- | --- | --- |
| P1 | PURCHASE E2E 자동 테스트 추가 | 수동 테스트 결과를 회귀 테스트로 고정해야 함 | signup부터 confirmPurchase 이후 정산까지 자동 검증 |
| P1 | 실패 케이스 테스트 | 정상 흐름 외 중복 수락/권한 없음/재확정 방어 확인 필요 | 크레딧 부족, 중복 수락, 권한 없음, 재확정 테스트 통과 |

## 6. P1 보강 작업

| 우선순위 | 작업 | 이유 |
| --- | --- | --- |
| P1 | `Idempotency-Key` 원본 blank 검증 | 현재 서버 prefix를 붙이면 원본 header가 blank여도 최종 key가 blank가 아니게 될 수 있음 |
| P1 | 무료 재능 가격 정책 확정 | `holdForEscrow()`는 amount <= 0을 거부하므로 0 크레딧 재능을 허용하면 수락 흐름이 실패할 수 있음 |
| P1 | 재능 조회 인증 정책 정리 | 컨트롤러는 사용자 식별이 없지만 Security 정책상 인증 필요 |
| P1 | Swagger 설명 인코딩 정리 | 일부 한글 설명이 깨져 보여 발표/시연 문서 품질에 영향 |
| P1 | AuthControllerTest 쿠키 `Secure` 기대값 이슈 분리 | 전체 테스트 실행 시 기존 쿠키 테스트 2건이 실패할 수 있음 |

## 7. MVP 이후 P1 필수 구현/고도화 대상

| 우선순위 | 작업 | 이유 |
| --- | --- | --- |
| P1 | SWAP 거래 확장 | Baton의 원래 핵심 방향인 양방향 재능 교환 구현 |
| P1 | 리뷰/신뢰 점수 | 거래 완료 이후 신뢰도 강화 기능 |
| P1 | 관리자 기능 | 운영 안정성 보강과 제출 완성도 확보 |
| P1 | 채팅 검증/고도화 | REST 채팅방/메시지와 WebSocket/STOMP 기본 구현은 완료. 거래 연결, 시연 범위, Redis Pub/Sub 도입 여부 검증 필요 |

## 8. P2 후순위

| 우선순위 | 작업 | 이유 |
| --- | --- | --- |
| P2 | 알림 | 사용성 보강. P1 필수 구현 이후 진행 |
| P2 | 유료 크레딧 충전 | 수익화/운영 확장. 발표 전 필수 흐름은 아님 |
| P2 | 고도화된 매칭 추천 | 매칭 품질 개선. 현재 추천 API 이후 개선 과제 |
| P2 | 모니터링/성능 고도화 | 운영 품질 개선. 제출용 최소 로그/메트릭 근거는 별도 확보 |

## 9. 팀장 관점 확인 사항

팀장이 바로 확인해야 할 것:

- 수정 코드가 반영된 서버를 재기동한 뒤 구매 확정 후 거래 상세 재조회가 `COMPLETED/RELEASED`인지 확인한다.
- Swagger/Postman 시연 순서에서 사용할 테스트 사용자/데이터와 ID 기록 방식을 확정한다.
- 재능 조회를 공개 API로 바꿀지, 인증 필요 API로 문서화할지 결정한다.
- 최종 테스트/커버리지와 배포 Swagger 결과를 확보한다.

팀원에게 맡길 것:

- PURCHASE E2E 통합 테스트 확장
- PURCHASE E2E 통합 테스트 작성
- 실패 케이스 테스트 작성
- Swagger 설명 인코딩 정리

다른 채팅으로 넘길 것:

- 최종 발표 문서/시연 대본 업데이트
- Postman 컬렉션 또는 Swagger 시연 순서 정리
- SWAP, 리뷰, 관리자 기능 설계
- 채팅 구현 범위와 시연 가능 범위 검증

## 10. 최종 판단

PURCHASE 거래의 핵심 도메인 연결은 코드 기준으로 거의 완성 단계다.

구매 확정 후 재조회 상태 저장 이슈는 코드 수정 및 통합 테스트로 해결 확인했다. 최종 제출용 MVP 완료 판단을 위해서는 수정 코드가 반영된 실행 서버에서 Swagger/Postman 재검증, 실패 케이스 테스트, 최종 커버리지 결과 확보가 남아 있다.
