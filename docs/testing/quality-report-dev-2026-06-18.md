# Baton 테스트 결과 및 커버리지 리포트

기준일: 2026-06-18  
기준 브랜치: `dev`  
실행 명령어: `.\gradlew.bat test jacocoTestReport`

## 1. 문서 기준

| 항목 | 내용 |
|---|---|
| 프로젝트 | Baton |
| 기준 브랜치 | `dev` |
| 기준 날짜 | 2026-06-18 |
| 테스트 기준 | Gradle test |
| 커버리지 기준 | JaCoCo |
| 테스트 리포트 | `build/reports/tests/test/index.html` |
| 커버리지 리포트 | `build/reports/jacoco/test/html/index.html` |

## 2. 테스트 실행 환경

| 항목 | 내용 |
|---|---|
| OS | Windows |
| JDK | Java 21 |
| Framework | Spring Boot |
| Build Tool | Gradle |
| 실행 Profile | local |
| DB | H2 in-memory |

## 3. 전체 테스트 결과

| 항목 | 결과 |
|---|---:|
| 테스트 클래스 수 | 22 |
| 전체 테스트 수 | 82 |
| 성공 | 82 |
| 실패 | 0 |
| 에러 | 0 |
| 스킵 | 0 |
| 실행 시간 | 15.79초 |
| 최종 결과 | 성공 |

## 4. JaCoCo 커버리지 결과

| 항목 | Covered | Missed | Total | Coverage |
|---|---:|---:|---:|---:|
| Instruction | 2233 | 197 | 2430 | 91.89% |
| Branch | 81 | 25 | 106 | 76.42% |
| Line | 487 | 51 | 538 | 90.52% |
| Complexity | 162 | 44 | 206 | 78.64% |
| Method | 134 | 19 | 153 | 87.58% |
| Class | 51 | 2 | 53 | 96.23% |

> QueryDSL 자동 생성 클래스인 `Q*.class`는 커버리지 집계에서 제외했다.

## 5. 도메인별 테스트 현황

| 도메인 | 테스트 클래스 | 테스트 수 | 상태 | 주요 검증 내용 |
|---|---|---:|---|---|
| Application | `BatonApplicationTests` | 1 | 통과 | Spring context 로딩 |
| User | `UserControllerTest`, `UserServiceTest` | 4 | 통과 | 회원가입 성공, DTO 검증, 비밀번호 정책 |
| Talent | Controller / Service / Repository 테스트 | 27 | 통과 | 재능 등록, 조회, 수정, 삭제, 권한, soft delete |
| Credit | `CreditControllerTest`, `CreditServiceTest` | 17 | 통과 | 잔액 조회, 계좌 없음, 충전/차감/에스크로 보류 |
| Matching | Controller / Entity / Service 테스트 | 17 | 통과 | 매칭 제안 생성, 수락, 상태 검증, 중복 검증 |
| Escrow | `EscrowTest`, `EscrowServiceTest` | 6 | 통과 | 에스크로 생성, 상태, 만료일, 정산 금액 |
| Trade | `TradeTest`, `TradeServiceTest` | 4 | 통과 | 거래 생성, 상태, 필드 저장 |
| Global | `GlobalExceptionHandlerTest` | 4 | 통과 | 공통 예외 응답 처리 |

## 6. 주요 테스트 시나리오

| ID | 도메인 | 시나리오 | 검증 포인트 | 상태 |
|---|---|---|---|---|
| TC-001 | User | 회원가입 성공 | 201 응답, 사용자 정보 반환 | 통과 |
| TC-002 | User | 회원가입 검증 실패 | 400 응답 | 통과 |
| TC-003 | Talent | 재능 등록 성공 | 201 응답, Location Header, talentId 반환 | 통과 |
| TC-004 | Talent | 재능 수정 권한 실패 | 403 응답 | 통과 |
| TC-005 | Talent | 재능 삭제 성공 | soft delete 처리 | 통과 |
| TC-006 | Credit | 크레딧 잔액 조회 성공 | `balance`, `escrowBalance` 반환 | 통과 |
| TC-007 | Credit | 크레딧 계좌 없음 | 404 응답 | 통과 |
| TC-008 | Matching | 매칭 제안 생성 성공 | 201 응답, `REQUESTED` 상태 | 통과 |
| TC-009 | Matching | 매칭 제안 수락 성공 | 200 응답, `ACCEPTED` 상태 | 통과 |
| TC-010 | Escrow | 에스크로 생성 | `HELD` 상태, 만료일, 금액 필드 저장 | 통과 |
| TC-011 | Trade | 거래 생성 | 거래 상태와 필드 저장 | 통과 |
| TC-012 | Global | 공통 예외 응답 | 표준 `ApiResponse` 실패 응답 | 통과 |

## 7. 실패 테스트 및 해결 내용

| ID | 테스트 | 실패 원인 | 해결 내용 | 현재 상태 |
|---|---|---|---|---|
| - | 해당 없음 | 테스트 실패 없음 | 해당 없음 | 전체 통과 |

## 8. 아직 부족한 테스트

| ID | 도메인 | 부족한 테스트 | 필요한 이유 | 우선순위 |
|---|---|---|---|---|
| TODO-001 | Trade/Escrow | MatchProposal 수락 이후 Trade/Escrow/Credit 연동 통합 테스트 | PURCHASE 핵심 거래 흐름 검증 | 높음 |
| TODO-002 | Credit | 동시 요청 시 잔액 정합성 테스트 | 중복 차감 및 잔액 불일치 방지 | 높음 |
| TODO-003 | Matching | 거절/취소 상태 전이 테스트 | enum은 있으나 API/흐름 보강 필요 | 중간 |
| TODO-004 | Auth | 로그인/인증 테스트 | 인증 기능 구현 후 필수 | 중간 |
| TODO-005 | Escrow | 정산/환불/분쟁 상태 테스트 | 에스크로 정책 고도화 필요 | 중간 |

## 9. 제출/발표용 요약

```text
dev 브랜치 기준 전체 테스트를 실행한 결과, 총 82개 테스트가 모두 통과했다.
JaCoCo 기준 Line Coverage는 90.52%, Branch Coverage는 76.42%로 확인되었다.
User, Talent, Credit, Matching, Escrow, Trade, Global 도메인을 중심으로 주요 Controller/Service/Entity 테스트가 작성되어 있다.
PURCHASE 핵심 흐름 중 MatchProposal 수락 이후 Trade/Escrow/Credit 연동 통합 테스트와 크레딧 동시성 테스트는 후속 보강 대상으로 분리한다.
```

## 10. 최종 체크리스트

- [x] 테스트 실행 명령어를 기록했다.
- [x] 전체 테스트 수, 성공, 실패, 스킵 수를 기록했다.
- [x] 테스트 리포트 위치를 기록했다.
- [x] JaCoCo 커버리지 수치를 기록했다.
- [x] 도메인별 테스트 현황을 정리했다.
- [x] 실패 테스트가 없음을 기록했다.
- [x] 부족한 테스트를 후속 보강 대상으로 분리했다.
- [x] 제출용 요약 문장을 작성했다.
