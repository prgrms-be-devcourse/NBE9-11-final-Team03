# Baton 테스트 결과 및 커버리지 리포트

기준일: 2026-06-18  
기준 브랜치: `dev`  
작성 목적: 중간 제출 시점의 테스트 실행 결과와 커버리지 상태를 정리하고, 부족한 테스트를 후속 작업으로 분리하기 위함.

## 1. 문서 기준

| 항목 | 내용 |
|---|---|
| 프로젝트 | Baton |
| 문서명 | 테스트 결과 및 커버리지 리포트 |
| 기준 브랜치 | `dev` |
| 기준 날짜 | 2026-06-18 |
| 테스트 기준 | Gradle test |
| 커버리지 기준 | JaCoCo |
| 테스트 리포트 위치 | `build/reports/tests/test/index.html` |
| 커버리지 리포트 위치 | `build/reports/jacoco/test/html/index.html` |

## 2. 테스트 실행 환경

| 항목 | 내용 |
|---|---|
| OS | Windows |
| JDK | Java 21 |
| Framework | Spring Boot |
| Build Tool | Gradle |
| 실행 Profile | local |
| DB | H2 in-memory |
| 기준 브랜치 | `dev` |

## 3. 테스트 실행 명령어

### 전체 테스트 실행

```powershell
.\gradlew.bat test
```

### 테스트 + JaCoCo 커버리지 실행

```powershell
.\gradlew.bat test jacocoTestReport
```

> 참고: JaCoCo 플러그인이 설정되어 있지 않으면 `jacocoTestReport` 작업이 실패할 수 있다.  
> 이 경우 먼저 `build.gradle.kts`에 JaCoCo 설정을 추가해야 한다.

## 4. 전체 테스트 결과

| 항목 | 결과 |
|---|---:|
| 전체 테스트 수 |  |
| 성공 |  |
| 실패 |  |
| 스킵 |  |
| 실행 시간 |  |
| 최종 결과 | 성공 / 실패 |

### 테스트 결과 요약

```text
dev 브랜치 기준 전체 테스트를 실행했다.
총 N개의 테스트 중 N개가 성공했고, 실패 N개, 스킵 N개가 확인되었다.
실패 테스트가 있는 경우 원인과 후속 처리 방안을 별도 기록했다.
```

## 5. 도메인별 테스트 현황

| 도메인 | 테스트 대상 | 테스트 클래스 | 주요 검증 내용 | 상태 | 비고 |
|---|---|---|---|---|---|
| User | 회원가입 Controller | `UserControllerTest` | 회원가입 성공, DTO 검증 실패 |  |  |
| Talent | 재능 등록 Controller | `TalentControllerTest` | 재능 등록 성공, 제목 검증 실패 |  |  |
| Talent | 재능 수정 Controller | `TalentControllerUpdateTest` | 수정 성공, 검증 실패, 권한 실패 |  |  |
| Talent | 재능 삭제 Controller | `TalentControllerDeleteTest` | 삭제 성공, 권한 실패, 미존재 재능 |  |  |
| Credit | 크레딧 잔액 조회 Controller | `CreditControllerTest` | 잔액 조회 성공, 계좌 없음 |  |  |
| Matching | 매칭 제안 Controller | `MatchProposalControllerTest` | 제안 생성, 제안 수락, 검증 실패 |  |  |

상태 값:

```text
통과
실패
스킵
보강 필요
미작성
```

## 6. 커버리지 결과

| 항목 | 수치 | 비고 |
|---|---:|---|
| 전체 Line Coverage |  |  |
| 전체 Branch Coverage |  |  |
| 전체 Method Coverage |  |  |
| 전체 Class Coverage |  |  |

## 7. 도메인별 커버리지

| 도메인 | Package | Line Coverage | Branch Coverage | 상태 | 비고 |
|---|---|---:|---:|---|---|
| User | `com.back.baton.domain.user` |  |  |  |  |
| Talent | `com.back.baton.domain.talent` |  |  |  |  |
| Credit | `com.back.baton.domain.credit` |  |  |  |  |
| Matching | `com.back.baton.domain.matching` |  |  |  |  |
| Escrow | `com.back.baton.domain.escrow` |  |  |  | Controller 없음 |
| Common | `com.back.baton.global` |  |  |  |  |

상태 값:

```text
충분
보통
보강 필요
측정 제외
```

## 8. 주요 테스트 시나리오

| ID | 도메인 | 시나리오 | 검증 포인트 | 상태 |
|---|---|---|---|---|
| TC-001 | User | 회원가입 성공 | 201 응답, 사용자 정보 반환 |  |
| TC-002 | User | 회원가입 DTO 검증 실패 | 400 응답 |  |
| TC-003 | Talent | 재능 등록 성공 | 201 응답, Location Header, talentId 반환 |  |
| TC-004 | Talent | 재능 수정 권한 실패 | 403 응답 |  |
| TC-005 | Talent | 재능 삭제 성공 | 200 응답, soft delete 요청 |  |
| TC-006 | Credit | 크레딧 잔액 조회 성공 | `balance`, `escrowBalance` 반환 |  |
| TC-007 | Credit | 크레딧 계좌 없음 | 404 응답 |  |
| TC-008 | Matching | 매칭 제안 생성 성공 | 201 응답, `REQUESTED` 상태 |  |
| TC-009 | Matching | 매칭 제안 수락 성공 | 200 응답, `ACCEPTED` 상태 |  |
| TC-010 | Matching | 매칭 제안 검증 실패 | 400 응답 |  |

## 9. 실패 테스트 및 해결 내용

| ID | 테스트 | 실패 원인 | 해결 내용 | 현재 상태 | 관련 PR/Jira |
|---|---|---|---|---|---|
| FAIL-001 |  |  |  |  |  |

작성 기준:

```text
실패 테스트가 없으면 "해당 없음"으로 작성한다.
실패가 있었다면 원인, 수정 내용, 재발 방지 방안을 함께 기록한다.
```

## 10. 아직 부족한 테스트

| ID | 도메인 | 부족한 테스트 | 필요한 이유 | 우선순위 | 후속 Jira |
|---|---|---|---|---|---|
| TODO-001 | Trade/Escrow | MatchProposal 수락 이후 Trade/Escrow 연동 테스트 | PURCHASE 흐름 핵심 | 높음 |  |
| TODO-002 | Credit | 동시 요청 시 잔액 정합성 테스트 | 크레딧 중복 차감 방지 | 높음 |  |
| TODO-003 | Matching | 거절/취소 상태 전이 테스트 | enum은 있으나 API 미구현 | 중간 |  |
| TODO-004 | Auth | 로그인/인증 테스트 | 현재 인증 미완성 | 중간 |  |
| TODO-005 | Escrow | 정산/환불/분쟁 상태 테스트 | 에스크로 정책 보강 필요 | 중간 |  |

## 11. 제출/발표용 요약

```text
dev 브랜치 기준 전체 테스트를 실행해 현재 구현된 API와 주요 도메인 로직의 동작을 확인했다.
User, Talent, Credit, Matching 도메인을 중심으로 Controller 테스트가 작성되어 있으며,
회원가입, 재능 등록/수정/삭제, 크레딧 잔액 조회, 매칭 제안 생성/수락 흐름을 검증했다.
Trade/Escrow/CreditTransaction 연동, 동시성, SWAP 정책 관련 테스트는 후속 보강 대상으로 분리했다.
```

## 12. 후속 Jira 티켓 후보

| 티켓명 | 구분 | 우선순위 | 설명 |
|---|---|---|---|
| `[QA] MatchProposal 수락 이후 Trade/Escrow 연동 테스트 추가` | 테스트 | 높음 | PURCHASE 핵심 흐름 검증 |
| `[QA] Credit 잔액 차감 동시성 테스트 추가` | 테스트 | 높음 | 중복 차감 방지 |
| `[QA] Matching 거절/취소 상태 전이 테스트 추가` | 테스트 | 중간 | `REJECTED`, `CANCELLED` 상태 검증 |
| `[QA] Escrow 정산/환불 상태 테스트 추가` | 테스트 | 중간 | Escrow 정책 보강 |
| `[QA] Auth 로그인/인증 테스트 추가` | 테스트 | 중간 | 인증 기능 구현 이후 추가 |

## 13. 최종 체크리스트

- [ ] 테스트 실행 명령어를 기록했다.
- [ ] 전체 테스트 수, 성공, 실패, 스킵 수를 기록했다.
- [ ] 테스트 리포트 위치를 기록했다.
- [ ] JaCoCo 커버리지 수치를 기록했다.
- [ ] 도메인별 테스트 현황을 정리했다.
- [ ] 실패 테스트가 있다면 원인과 해결 내용을 기록했다.
- [ ] 부족한 테스트를 후속 Jira 티켓으로 분리했다.
- [ ] 제출용 요약 문장을 작성했다.
