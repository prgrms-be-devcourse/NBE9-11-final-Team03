# Baton 최종 제출물 갭 체크리스트

> 문서 버전: v1.3
> 기준일: 2026-06-23
> 제출 마감: 2026-07-01 자정 전
> Notion 재확인 기준: `최종 결과물 제출 마감 (자정)` 페이지
> 문서 상태: 진행 관리 중, MVP 이후 P1 필수 구현 범위 반영
> 관리 원칙: 제출 항목, 담당, 링크, 검증 결과가 확정될 때마다 갱신한다.

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-21 | 최종 제출물 필수 항목과 갭 체크리스트 작성 | 작성 완료 |
| v1.1 | 2026-06-22 | 문서 버전/상태 추가 및 PR #63 관련 작업 업데이트 기준 명시 | 진행 관리 중 |
| v1.2 | 2026-06-22 | PR #62/#64 병합 완료와 #67/#68 진행 상태 반영 | 진행 관리 중 |
| v1.3 | 2026-06-23 | MVP 수동 API 테스트, 실제 응답 기록 문서, 구매 확정 재조회 이슈 반영 | 최신 테스트 기준 |
| v1.4 | 2026-06-23 | SWAP/관리자/리뷰/채팅을 P1 필수 구현/고도화 범위로 정리 | 최신 총괄 기준 |

## 1. 현재 총괄 판단

Notion 최종 제출 페이지 기준으로 제출물은 단순한 발표자료만이 아니라 `문서 + 코드 + 테스트 결과 + CI/CD 결과 + 모니터링/부하테스트 결과 + 시연 영상 + 배포 링크 + 팀별 대시보드 + 구글 폼 제출`까지 포함한다.

현재 Baton 로컬 문서에는 프로젝트 개요, 기획서, 발표 구성, 데모 스크립트, 시스템 아키텍처, 회고 문서가 이미 있다. 하지만 Notion 기준으로 보면 아직 부족한 축이 있다.

가장 큰 갭은 다음이다.

1. 최종 제출 페이지에 넣을 링크 인덱스
2. 원격 dev 기준 실제 구현 상태가 반영된 최종 API 명세서
3. JaCoCo/JUnit 테스트 커버리지 결과
4. CI/CD 파이프라인 성공 로그와 구성 코드 설명
5. 메트릭 수집, 모니터링, 부하테스트 결과
6. Postman/Swagger UI 기반 API 호출 시연 영상과 YouTube 링크
7. 배포 링크와 팀별 대시보드 정리

## 2. Notion 필수 제출 항목 정리

| 제출 항목 | 필수 여부 | 현재 대응 자료 | 추가로 해야 할 일 |
| --- | --- | --- | --- |
| 발표자료 PPT | 필수 | `presentation-outline.md` | 고용노동부 지정 PPT 양식에 맞춰 실제 PPT 작성 |
| 결과보고서 | 필수 | `project-master.md`, `notion-project-plan-v4.md` | PPT 안에 결과보고서 항목 포함 |
| 프로젝트 개요 | 필수 | `project-master.md`, `notion-project-plan-v4.md` | v2/v3 통합 서사 최종 정리 |
| 팀 구성 및 역할 | 필수 | 회의 문서 일부 | 팀원별 실제 담당 기능/산출물 표 작성 |
| 프로젝트 수행 절차 및 방법 | 필수 | `project-master.md` 일부 | 기획, 설계, 개발, 검증, 배포 순서로 정리 |
| 프로젝트 수행 경과 | 필수 | Jira/PR/회의록 | 날짜별 진행 경과와 주요 PR 정리 |
| 최종 API 명세서 | 필수 | `docs/api/api-spec-v1.md`, Swagger | 구매 확정 재조회 이슈 해결 후 최종 상태 전이 재확인 |
| JUnit 테스트 커버리지 리포트 | 필수 | 테스트 코드, JaCoCo 설정 확인 필요 | 테스트 실행 후 수치/HTML 리포트/캡처 확보 |
| CI/CD 파이프라인 로그 결과 | 필수 | GitHub Actions | 성공 로그 캡처, workflow 파일 링크, 실패/해결 이력 정리 |
| CI/CD 파이프라인 구성 코드 | 필수 | `.github/workflows` 확인 필요 | workflow 주요 단계 설명 |
| 메트릭 수집/모니터링/부하테스트 결과 | 필수 | 부족 | Prometheus/Grafana 또는 대체 결과 자료 필요 |
| Postman/Swagger UI 기반 API 호출 시연 영상 | 필수 | `demo-script.md`, `mvp-api-test-request-response-log.md` | PURCHASE MVP 전체 흐름 녹화 |
| UI 기반 시연 영상 | 선택 | 불명확 | UI가 없으면 선택 항목으로 제외, 있으면 보조 영상 |
| 자체 평가 의견 및 회고 | 필수 | `self-evaluation-retrospective.md` | 최종 구현 결과와 팀 회고 반영 |
| 시연 영상 YouTube 링크 | 필수 | 없음 | 공개 권한 확인 후 링크 제출 |
| 배포 링크 | 필수 | AWS 배포 상태 확인 필요 | 서버 배포 URL, Swagger URL 정리 |
| 팀별 대시보드 | 필수 | Notion 팀 페이지 | 배포 링크, 발표 자료, 시연 영상 링크 삽입 |
| 구글 폼 제출 | 필수 | 없음 | 2026-07-01 자정 전 제출 |

## 3. P0: 오늘 회의에서 바로 정해야 할 것

| 안건 | 결정할 내용 | 추천 담당 |
| --- | --- | --- |
| 최종 제출 페이지 | 어떤 링크를 어디에 모을지 | 남진우 |
| 발표자료/결과보고서 | PPT 담당, 마감, 포함 항목 | 남진우 + 전원 |
| 최종 API 명세서 | 최신 API 반영 담당 | 각 도메인 담당자 |
| 테스트 커버리지 | JaCoCo 실행, 수치 캡처, 리포트 링크 | 테스트 담당 또는 최윤서 |
| CI/CD 결과 | GitHub Actions 성공 로그와 workflow 설명 | 인프라/배포 담당 |
| 모니터링/부하테스트 | 실제 구현할지, 대체 자료로 정리할지 | 인프라/배포 담당 |
| API 시연 영상 | Swagger/Postman 녹화 담당 | 남진우 + 백엔드 전원 |
| 배포 링크 | AWS 배포 상태와 URL 확정 | 인프라/배포 담당 |
| 구글 폼 제출 | 최종 제출자 확정 | 남진우 |

## 4. P1: 이번 주 안에 완성해야 할 제출물

| 날짜 | 제출물 목표 |
| --- | --- |
| 2026-06-21 | 제출물 담당자, 마감, 완료 기준 확정 |
| 2026-06-22 | 프로젝트 개요, 팀 역할, 수행 절차 초안 |
| 2026-06-23 | API 명세서 최신화, Jira/PR 수행 경과 정리 |
| 2026-06-24 | 테스트 커버리지/CI 로그 1차 확보 |
| 2026-06-25 | Swagger/Postman 시연 순서 확정 |
| 2026-06-26 | 발표자료 PPT 1차 완성 |
| 2026-06-27 | 시연 영상 녹화 1차본 |
| 2026-06-28 | 기능 범위 동결, 추가 기능 제출 포함 여부 확정 |
| 2026-06-29 | 최종 제출 페이지와 팀별 대시보드 1차 완성 |
| 2026-06-30 | 링크/권한/영상/배포 URL 검수, 리허설 |
| 2026-07-01 | 구글 폼 제출, 제출 상태 최종 확인 |

## 5. P1 필수 구현/고도화 제출 기준

추가 기능이라는 표현을 쓰더라도 SWAP, 관리자, 리뷰/신뢰 점수, 채팅은 최종 완성도를 위해 관리할 P1 범위다. 최종 문서에서는 `구현 완료`, `부분 완료`, `확장 계획`을 분리한다.

| 추가 기능 | 제출에 넣는 기준 |
| --- | --- |
| 관리자 기능 | Swagger 또는 화면에서 최소 1개 관리 흐름이 동작하면 구현 완료 |
| SWAP 거래 | 제안, 수락, 거래 생성, 정산 정책까지 실제 API가 연결되면 구현 완료 |
| 리뷰/신뢰 점수 | 거래 완료 후 리뷰 작성/조회가 가능하면 구현 완료 |
| 채팅 | REST 채팅방/메시지와 WebSocket/STOMP 송수신 기본 구현은 완료. 실제 시연 가능한 호출 흐름과 거래 연결 여부를 표시 |
| S3/재능 첨부파일 | 재능 등록/수정과 파일 업로드 흐름이 연결되면 구현 완료 |
| 알림/유료 충전/고도화 추천 | 시간이 부족하면 확장 계획으로 분리 |

## 6. 완료 판단 기준

최종 제출 준비는 다음 조건을 만족하면 완료로 본다.

- 최종 제출 페이지에서 GitHub, 발표자료, API 명세서, 시연 영상, 배포 링크로 이동 가능하다.
- 팀별 대시보드에 배포 링크, 발표 자료, 시연 영상 링크가 들어가 있다.
- 발표자료 안에 프로젝트 개요, 팀 구성, 수행 절차, 수행 경과, API 명세, 테스트 커버리지, CI/CD, 모니터링/부하테스트, 시연 영상, 회고가 포함되어 있다.
- Swagger/Postman API 호출 시연 영상이 YouTube에 올라가 있고 공개 권한이 확인되어 있다.
- 배포 링크가 접속 가능하다.
- 구글 폼 제출 담당자가 정해져 있고, 2026-07-01 자정 전 제출한다.

## 7. 작업 업데이트 (2026-06-22)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| BATON-88 인증 사용자 주입 방식 통합 | 완료 | PR #63 생성 |
| 원격 dev 최신화 | 완료 | `origin/dev` 최신 커밋 `ff159a7` 기준 rebase |
| 브랜치 정리 | 완료 | `refactor/BATON-88-current-user` |
| JWT 인증 principal 정리 | 완료 | `JwtAuthenticationFilter`에서 `SecurityUser` 저장 |
| 컨트롤러 userId 직접 입력 제거 | 완료 | `@CurrentUser SecurityUser` 기준으로 통합 |
| Auth/User 컨트롤러 반영 | 완료 | `AuthController.logout`, `UserController.withdraw` 적용 |
| 테스트 principal 정리 | 완료 | `@WithMockSecurityUser` 테스트 지원 추가 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| PR #63 CI 확인 | 팀장 확인 | GitHub Actions 결과 확인 필요 |
| PR #63 리뷰 대응 | 백엔드 담당 | 리뷰 코멘트 발생 시 반영 |
| refreshToken 쿠키 테스트 이슈 분리 | 팀장 판단 | `AuthControllerTest`의 `Secure` 기대값 불일치 2건 |
| 팀원 공유 | 팀장 | 앞으로 컨트롤러에서 `@CurrentUser SecurityUser` 사용 |

### P2 주의 사항

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| 문서 폴더 untracked | 유지 | `docs/api`, `docs/erd`, `docs/final`, `docs/refactoring`, `docs/reviews` |
| PR #63 범위 | 코드/테스트만 포함 | 문서 폴더는 PR에 포함하지 않음 |
| 전체 테스트 | 일부 실패 존재 | 쿠키 `Secure` 테스트 이슈는 CurrentUser 리팩터링과 별도 |

## 8. 작업 업데이트 (2026-06-22, PR #63 커밋 분리 후)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| PR #63 커밋 분리 | 완료 | 3개 커밋으로 재구성 |
| 인증 사용자 기반 구조 커밋 분리 | 완료 | `e027706 BATON-88 refactor: 인증 사용자 principal 구조 추가` |
| 컨트롤러 적용 커밋 분리 | 완료 | `662d515 BATON-88 refactor: 컨트롤러 인증 사용자 주입 방식 통합` |
| 테스트 수정 커밋 분리 | 완료 | `c005694 BATON-88 test: SecurityUser 기반 컨트롤러 테스트 수정` |
| PR 브랜치 갱신 | 완료 | `refactor/BATON-88-current-user` force-with-lease push |
| PR 템플릿 본문 수정 | 완료 | Jira `BATON-88` 링크 포함 |
| Swagger 영어 설명 복구 | 완료 | Operation/Tag/Parameter/Schema 문구 한국어 복구 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| PR #63 CI 결과 확인 | 팀장 | GitHub Actions 통과 여부 확인 |
| PR #63 리뷰 요청 | 팀장 | 커밋이 3개로 분리되었음을 리뷰어에게 공유 |
| PR #63 리뷰 대응 | 백엔드 담당 | 누락된 컨트롤러 userId 처리 여부 확인 필요 |
| refreshToken 쿠키 테스트 이슈 분리 | 팀장 판단 | `AuthControllerTest`의 `Secure` 기대값 불일치 2건 |

### P2 주의 사항

| 항목 | 상태 | 비고 |
| --- | --- | --- |
| 문서 폴더 untracked | 유지 | 현재 PR에는 포함하지 않음 |
| 전체 테스트 | 일부 실패 가능 | 쿠키 `Secure` 테스트 이슈는 BATON-88 리팩터링과 별도 |
| PR 히스토리 | 변경됨 | 기존 단일 커밋에서 3개 커밋으로 force push 완료 |

## 9. 작업 업데이트 (2026-06-22, MVP 달성도 점검)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| PR #63 포함 기준 MVP 달성도 점검 | 완료 | `docs/final/mvp-achievement-status-2026-06-22.md` 작성 |
| PURCHASE 거래 연결 상태 재확인 | 완료 | 매칭 수락 -> Trade -> Credit hold -> Escrow -> Submit -> Confirm -> Settle 흐름 확인 |

### 당시 P0 남은 일과 현재 상태

| 항목 | 현재 상태 | 완료 기준/비고 |
| --- | --- | --- |
| 회원가입 후 초기 크레딧 자동 지급 연결 | 완료 | `AuthService.signup()` 이후 `CreditService.initializeAccount(userId)` 호출 및 테스트 완료 |
| CreditTransaction 조회 API | 완료 | `/api/v1/credit/transactions`로 Swagger/Postman 시연에서 이력 확인 |
| PURCHASE E2E 통합 테스트 | 수동 테스트 완료, 자동화 필요 | 가입부터 구매 확정/정산까지 수동 API 테스트 완료. 자동 통합 테스트는 P1 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| Idempotency-Key 원본 blank 검증 | 백엔드 | 서버 prefix를 붙이기 전 header 원본 검증 필요 |
| 무료 재능 가격 정책 확정 | 팀장 | 0 크레딧 허용 시 현재 에스크로 보류 로직과 충돌 가능 |
| Swagger 설명 인코딩 정리 | 백엔드/문서 | 일부 설명이 깨져 보여 발표 전 정리 필요 |

## 10. 작업 업데이트 (2026-06-22, BATON-88 병합 완료)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| BATON-88 인증 사용자 주입 방식 통합 PR 병합 | 완료 | PR #63 병합 완료 |
| 컨트롤러 사용자 식별값 직접 입력 제거 | 완료 | `@CurrentUser SecurityUser` 기준 적용 |
| JWT 인증 principal 구조 정리 | 완료 | `JwtAuthenticationFilter`에서 `SecurityUser`를 `SecurityContextHolder`에 저장 |
| 컨트롤러 테스트 principal 정리 | 완료 | `@WithMockSecurityUser` 테스트 지원 추가 |

### 당시 P0 남은 일과 현재 상태

| 항목 | 현재 상태 | 완료 기준/비고 |
| --- | --- | --- |
| 회원가입 후 초기 크레딧 자동 지급 PR 확인 | 완료 | PR #62 머지 후 `AuthService.signup()`에서 `CreditService.initializeAccount(userId)` 호출 확인 |
| 진행 중 거래 재능 삭제 차단 PR 수정 확인 | 완료 | PR #64에서 `IN_PROGRESS`, `UNDER_REVIEW` 차단 반영 |
| PURCHASE E2E 통합 테스트 | 수동 테스트 완료, 자동화 필요 | 가입부터 구매 확정/정산까지 수동 API 테스트 완료. 자동 통합 테스트는 P1 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| Swagger 사용자 식별값 명세 재점검 | 백엔드/문서 | BATON-88 병합 후 `userId`, `buyerId`, `sellerId` 직접 입력이 남았는지 확인 |
| refreshToken 쿠키 테스트 이슈 분리 | 팀장 판단 | `AuthControllerTest`의 `Secure` 기대값 불일치 2건은 BATON-88과 별도 |
| CreditTransaction 조회 API | 완료 | `/api/v1/credit/transactions`로 Swagger/Postman 시연에서 이력 확인 |

## 11. 작업 업데이트 (2026-06-22, 문서 역할 분리)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| 최종 제출 문서 역할별 폴더 분리 | 완료 | `docs/final/overview`, `status`, `presentation`, `architecture`, `retrospective`, `templates`로 정리 |
| 회의 문서 별도 관리 | 완료 | `docs/meetings`로 회의 안건/진행 문서/회의록 이동 |
| 문서 안내 README 추가 | 완료 | `docs/final/README.md`, `docs/meetings/README.md` 작성 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| MVP 달성도 문서 갱신 | 백엔드/팀장 | PR #62, PR #64 머지 여부 반영 |

### P1 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| API 명세 최신화 | 완료 | `docs/api/api-spec-v1.md`를 `@CurrentUser`, Trade/Submission/S3 API 기준으로 갱신 |
| 시연 스크립트 최신화 | 완료 | `docs/final/presentation/demo-script.md`에서 예전 query/header 사용자 식별 흐름 제거 |
| 시스템 구성도 최신화 | 완료 | `docs/final/architecture/system-architecture.md`에서 Trade/Credit/Escrow TODO 문구 제거 |
| 프로젝트 총괄 문서 최신화 | 완료 | `docs/final/overview/project-master.md`에 최신 MVP 상태와 팀 역할 반영 |
| 기획서 v4.1 피드백 반영 | 진행 | 진행 상태, 일정 준수, 상태 전이, 책임 경계, 동시성 전략 추가 |

## 12. 작업 업데이트 (2026-06-22, PR 상태 재확인)

### 해결된 일

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| 회원가입 후 초기 크레딧 자동 지급 확인 | 완료 | PR #62 `BATON-48 feat: 회원가입시 계좌 생성, 탈퇴한 회원 정책 반영` 병합, CI 성공 |
| 진행 중 거래 재능 삭제 차단 확인 | 완료 | PR #64 `BATON-51 [후속] 진행 중인 거래가 존재할 경우 재능 삭제 차단 로직 추가` 병합, CI 성공 |
| BATON-88 인증 사용자 주입 방식 통합 | 완료 | PR #63 병합 완료 |
| CreditTransaction 조회 API | 완료 | `/api/v1/credit/transactions` 구현 및 수동 API 테스트 확인 |

### 아직 남은 일

| 항목 | 상태 | 다음 행동 |
| --- | --- | --- |
| PURCHASE E2E 통합 테스트 | 남음 | 가입 또는 계좌 준비부터 구매 확정/정산까지 한 테스트로 검증 |
| PR #67 크레딧 거래 내역 조회 | 리뷰/머지 대기 | CI 성공 상태이므로 리뷰 후 병합 확인 |
| PR #68 SecurityConfig 인증/CORS 수정 | 리뷰/머지 대기 | CI 성공 상태이므로 Swagger 접근, CORS origin 설정 리뷰 후 병합 확인 |
| Swagger 사용자 식별값 명세 재점검 | 남음 | `@CurrentUser` 병합 이후 직접 입력 받는 사용자 식별값이 남았는지 확인 |

## 13. 작업 업데이트 (2026-06-22, MatchProposal 수락 흐름 문서화)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| MatchProposal 수락 시퀀스 문서화 | 완료 | `docs/final/architecture/match-proposal-accept-flow.md` 작성 |
| 수락 흐름 요청/응답 정리 | 완료 | `proposalId`, `Idempotency-Key`, `@CurrentUser providerId`, `ApiResponse<MatchProposalRes>` 기준 |
| 동시성/락 전략 정리 | 완료 | 트랜잭션, unique 제약, 조건부 update, lock 보강 후보 표 작성 |
| 상태 전이와 책임 경계 정리 | 완료 | Matching, Trade, Escrow, Credit 책임 분리 표 작성 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| MatchProposal 수락 동시성 테스트 | 백엔드 | accept/reject 동시 요청, 중복 accept 요청 검증 |
| MatchProposal lock 적용 여부 결정 | 백엔드 | `findByIdWithLock()` 도입 필요성 검토 |
| 멱등키 요청 동일성 검증 | 백엔드 | 같은 key가 같은 `userId/tradeId/amount` 요청인지 확인하는 보강 검토 |

## 14. 작업 업데이트 (2026-06-23, MVP 정상 흐름 API 테스트)

### P0 완료

| 항목 | 상태 | 근거 |
| --- | --- | --- |
| MVP PURCHASE 정상 흐름 수동 API 테스트 | 완료 | localhost:8080 기준 회원가입 -> 로그인 -> 초기 크레딧 -> 재능 등록 -> 매칭 제안 생성 -> 수락 -> Trade/Escrow 생성 -> 결과물 제출 -> 구매 확정 -> 정산 -> CreditTransaction 조회까지 성공 |
| Swagger/Postman 재현용 테스트 기록 문서 작성 | 완료 | `docs/testing/mvp-purchase-flow-test-2026-06-23.md` 작성 |
| 회원가입 후 초기 크레딧 지급 확인 | 완료 | 구매자/제공자 신규 가입 직후 balance 10000, escrowBalance 0 확인 |
| 매칭 수락 후 크레딧 에스크로 보관 확인 | 완료 | 구매자 balance 5000, escrowBalance 5000, Trade IN_PROGRESS, Escrow HELD 확인 |
| 구매 확정 후 정산 확인 | 완료 | Trade COMPLETED, Escrow RELEASED, 구매자 escrowBalance 0, 제공자 balance 15000 확인 |
| CreditTransaction 기록 확인 | 완료 | 구매자 WELCOME/ESCROW_HOLD/ESCROW_RELEASE, 제공자 WELCOME/ESCROW_RELEASE 조회 확인 |
| 구매 확정 후 Trade/Escrow 상태 저장 이슈 수정 | 완료 | `CreditAccountRepository` 벌크 업데이트 flush 설정 수정 및 통합 테스트 통과 |
| 수정 코드 반영 서버 API 재검증 | 완료 | confirm 직후 `GET /api/v1/trade/1` 응답이 `COMPLETED/RELEASED`로 반환됨 |

### P0 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| 최종 시연용 데이터/순서 재확인 | 백엔드/QA | 발표 전 사용할 계정, `talentId`, `proposalId`, `tradeId`를 새로 기록 |

### P1 남은 일

| 항목 | 담당/확인 | 비고 |
| --- | --- | --- |
| 재능 상세 조회 인증 정책 확인 | 백엔드/API | 비로그인 `GET /api/v1/talents/{talentId}`는 403 발생, 인증 토큰 포함 시나리오 기준으로 문서/Swagger 정리 필요 |
| PURCHASE 실패 케이스 테스트 | 백엔드/QA | 크레딧 부족, 재수락, 권한 없는 수락, 재확정, idempotency 재사용 |

## 작업 업데이트 (2026-06-23, 구매 확정 상태 저장 이슈)

### 완료

| 우선순위 | 완료 항목 | 근거 |
|---|---|---|
| P0 | 구매 확정 후 Trade/Escrow 상태 저장 누락 원인 수정 | `CreditAccountRepository` 벌크 업데이트에 `flushAutomatically = true` 추가 |
| P0 | 구매 확정/거래 취소 후 DB 재조회 통합 테스트 추가 | `TradeSettlementPersistenceIntegrationTest` 추가 |
| P0 | 구매 확정/거래 취소 저장 상태 테스트 통과 | `./gradlew.bat test --rerun-tasks --tests "com.back.baton.domain.trade.service.TradeSettlementPersistenceIntegrationTest"` 결과 `BUILD SUCCESSFUL` |
| P0 | MVP 테스트 내역 최신화 | `docs/testing/mvp-purchase-flow-test-2026-06-23.md`에 후속 코드 검증 섹션 추가 |

### 남은 일

| 우선순위 | 해야 할 일 | 완료 기준 |
|---|---|---|
| P0 | 서버 재기동 후 Swagger/Postman API 재검증 | 완료. 실제 API에서 confirm 직후 `GET /api/v1/trade/1`가 `COMPLETED/RELEASED`로 응답 |
| P1 | 거래 취소 후 API 재조회 검증 | 통합 테스트는 완료. 서버 API에서 cancel 직후 `GET /api/v1/trade/{tradeId}`가 `CANCELLED/REFUNDED`로 응답하는지 확인 |
| P1 | PURCHASE E2E 자동 테스트 확장 | 회원가입부터 구매 확정/정산/이력 조회까지 자동화 |
