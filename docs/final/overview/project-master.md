# Baton 프로젝트 총괄 문서

> 문서 버전: v1.4
> 기준일: 2026-06-23
> 기준 브랜치: `dev`
> 기준 PR: `#62`, `#63`, `#64`, `#67`, `#68` 반영 기준
> 문서 상태: MVP 수동 API 테스트 결과와 MVP 이후 필수 구현 범위 반영
> 프로젝트: Baton
> 팀: Team3
> 문서 목적: 최종 보고서, 발표자료, 시연 스크립트의 기준 원고로 사용할 프로젝트 총괄본

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v1.0 | 2026-06-20 | 최초 총괄 문서 작성 | 작성 완료 |
| v1.1 | 2026-06-22 | 문서 버전/기준 브랜치/문서 상태 추가 | 구현 반영 필요 |
| v1.2 | 2026-06-22 | 최신 컨트롤러/API 기준으로 MVP 상태와 리스크 갱신 | 최신 구현 기준 요약 |
| v1.3 | 2026-06-22 | PR 상태 재확인 후 완료/남은 작업 갱신 | 진행 관리 중 |
| v1.4 | 2026-06-23 | MVP 수동 API 테스트, 초기 크레딧/거래내역 조회, 구매 확정 재조회 이슈 반영 | 최신 테스트 기준 |
| v1.5 | 2026-06-23 | SWAP/관리자/리뷰/채팅의 MVP 이후 필수 구현 범위 반영 | 최신 총괄 기준 |

## 1. 문서/발표 판단

Baton은 돈이 부족한 주니어가 자신의 재능을 기반으로 필요한 도움을 얻을 수 있도록 돕는 크레딧 기반 재능 거래/교환 서비스다. 발표와 제출 문서의 중심은 `PURCHASE MVP`이지만, SWAP, 관리자, 리뷰/신뢰 점수, 채팅은 단순 선택 고도화가 아니라 최종 완성도를 위해 이어서 관리할 P1 필수 구현/고도화 범위로 둔다.

현재 이 문서는 최종 Canva 발표자료를 직접 대체하지 않는다. 대신 Canva에 옮길 수 있는 구조, 문장, 표, 시연 흐름의 원천 문서 역할을 한다.

## 2. 프로젝트 개요

| 항목 | 내용 | 상태 |
|---|---|---|
| 서비스명 | Baton | 확정 |
| 핵심 콘셉트 | 재능을 등록하고 내부 크레딧으로 다른 사람의 재능을 구매/교환하는 서비스 | 확정 |
| 핵심 대상 | 돈은 부족하지만 배울 것과 나눌 재능이 있는 주니어 사용자 | 기획서 원문 확인 필요 |
| MVP 중심 | 회원가입/로그인부터 PURCHASE 거래, 에스크로, 정산, 크레딧 이력까지의 흐름 | 정상 흐름 검증 완료, 재조회 상태 저장 이슈 코드 수정 및 통합 테스트 완료 |
| 발표 방향 | 문제 제기 -> 해결 방식 -> MVP 흐름 -> 기술 구현 -> 테스트/배포 -> 회고 | 확정 |

## 3. 문제 정의와 핵심 가치

### 문제 정의

주니어 개발자, 디자이너, 기획자, 취업 준비생은 서로에게 필요한 역량을 갖고 있지만 현금 기반 외주나 과외 시장에 진입하기 어렵다. 또한 개인 간 거래에서는 선결제 후 미이행, 일방적 이탈, 결과물 품질 불확실성 같은 신뢰 문제가 발생한다.

### 해결 방식

Baton은 사용자가 자신의 재능을 등록하고, 필요한 재능을 가진 사용자에게 매칭 제안을 보내며, 거래 금액을 내부 크레딧과 에스크로로 관리한다. 구매자는 거래 시작 시 크레딧을 예치하고, 거래 완료 후 제공자에게 정산된다.

### 차별화 포인트

| 포인트 | 설명 | 발표 사용 여부 |
|---|---|---|
| 크레딧 기반 거래 | 현금 부담을 낮추고 서비스 안에서 교환 가능한 단위를 제공 | 사용 |
| 에스크로 구조 | 거래 중 크레딧을 보류해 미이행 위험을 낮춤 | 사용 |
| 재능 기반 매칭 | 사용자가 가진 재능과 필요한 재능을 연결 | 사용 |
| SWAP 확장성 | PURCHASE 안정화 이후 양방향 재능 교환으로 확장 | P1 필수 구현 목표 |
| 신뢰 점수/리뷰 | 거래 품질과 사용자 신뢰를 누적 | P1 필수 구현 목표 |

## 4. MVP 성공 기준

P0 기준 성공 흐름은 아래 순서로 정의한다.

1. 회원가입/로그인
2. 초기 크레딧 지급
3. 재능 등록/조회
4. 매칭 제안 생성
5. 매칭 제안 수락
6. PURCHASE 거래 생성
7. 구매자 크레딧 차감
8. 에스크로 보관
9. 거래 상태 조회
10. 거래 완료/구매 확정
11. 제공자 크레딧 정산
12. 크레딧 변동 내역 기록

### 현재 근거 기준 상태

| 흐름 | 코드/문서 근거 | 상태 | 발표 리스크 |
|---|---|---|---|
| 회원가입 | `AuthController.signup` | 구현 확인 | 낮음 |
| 로그인/토큰 재발급 | `AuthController.login`, `AuthController.reissue` | 구현 확인, 테스트 일부 실패 | 중간 |
| 초기 크레딧 | `AuthService.signup`, `CreditService.initializeAccount` | 회원가입 자동 연동 및 WELCOME 이력 검증 완료 | 낮음 |
| 재능 등록/조회 | `TalentController` | 구현 확인 | 낮음 |
| 매칭 추천 | `MatchRecommendationController` | 구현 확인 | 낮음 |
| 매칭 제안 생성/수락/거절 | `MatchProposalController`, `MatchProposalService` | 수락 시 Trade/Credit/Escrow 연결 구현/검증 | 낮음 |
| 거래 조회/취소 | `TradeController` | 조회/취소 구현 | 낮음 |
| 결과물 제출/구매 확정 | `TradeController`, `TradeSubmissionService` | 제출/조회/구매 확정 구현 | S3/시연 데이터 준비 필요 |
| 에스크로 생성 | `EscrowService.create` | 매칭 수락 흐름에서 생성 | 낮음 |
| 크레딧 예치/환불/이력 | `CreditService`, `CreditTransaction` | 서비스/엔티티/조회 API 구현 | 구매자 `ESCROW_RELEASE` 표기 의미 설명 필요 |
| 거래 완료/정산 | `TradeSubmissionService.confirmPurchase`, `CreditService.settleEscrow` | 구현/정산 검증 | 상태 저장 이슈 코드 수정 완료, 서버 API 재검증 필요 |

## 5. 구현 기능 정리

### P0: MVP 핵심

| 도메인 | 주요 기능 | 상태 |
|---|---|---|
| Auth/User | 회원가입, 로그인, refresh token 재발급 | 구현 확인 |
| Talent | 재능 등록, 수정, 삭제, 목록, 검색, 상세 | 구현 확인 |
| Matching | 추천 목록, 추천 상세, 제안 생성, 수락, 거절 | 구현 확인 |
| Credit | 잔액 조회, 초기 계좌 생성, 적립, 차감, 에스크로 보류, 환불, 정산, 이력 기록/조회 | 정상 흐름 검증 완료 |
| Escrow | 에스크로 생성, 환불, 정산 상태 관리 | 매칭 수락/구매 확정 흐름에 연결 |
| Trade | 거래 생성, 조회, 취소, 결과물 제출, 구매 확정 | 정상 흐름 검증 완료, 구매 확정/취소 후 DB 재조회 통합 테스트 완료 |

### P1: MVP 이후 필수 구현/고도화 대상

| 기능 | 발표 포지션 | 상태 |
|---|---|---|
| 관리자 기능 | 운영 안정성 확장 | 필수 구현 목표. 최소 관리자 조회/상태 관리 흐름 확정 필요 |
| SWAP 거래 | Baton의 핵심 차별화 기능 | 필수 구현 목표. PURCHASE 2건과 거래 그룹 기반 구조 검토 |
| 리뷰/신뢰 점수 | 거래 신뢰도 강화 | 필수 구현 목표. 거래 완료 후 리뷰 작성/조회와 중복 방지 정책 필요 |
| 채팅 | 거래 전후 커뮤니케이션 보강 | 기본 구현 완료. REST 채팅방/메시지와 WebSocket/STOMP 송수신 기준 검증, Redis Pub/Sub는 고도화 |

### P2: 후순위 고도화

| 기능 | 발표 포지션 | 상태 |
|---|---|---|
| 알림 | 사용성 고도화 | 후순위 |
| 유료 크레딧 충전 | 수익화/운영 확장 | 후순위 |
| 고도화된 추천 | 매칭 품질 개선 | 후순위 |
| 성능/모니터링 | 운영 품질 개선 | 후순위 |

## 6. 기술 스택과 선택 근거

| 영역 | 기술 | 발표 포인트 |
|---|---|---|
| Backend | Spring Boot, Java 21 | 안정적인 REST API와 도메인 중심 개발 |
| Persistence | JPA, QueryDSL | 엔티티 기반 개발과 검색/추천 쿼리 확장 |
| DB | H2, MySQL | 로컬 개발/테스트와 운영 DB 분리 |
| Auth | Spring Security, JWT | access token, refresh token 기반 인증 구조 |
| Test | JUnit, JaCoCo | 도메인별 단위/컨트롤러 테스트와 커버리지 관리 |
| API Docs | Swagger/OpenAPI | 시연 가능한 API 문서화 |
| CI/CD | GitHub Actions, Docker, EC2 | 빌드/테스트 자동화와 컨테이너 기반 배포 |

## 7. 시스템 구성 요약

로컬 개발은 H2 in-memory DB와 Swagger UI를 중심으로 검증한다. 운영 배포는 GitHub Actions에서 Gradle 빌드 후 Docker 이미지를 생성하고, Docker Hub와 EC2를 통해 배포하는 구조로 설계되어 있다.

| 구성 | 근거 | 상태 |
|---|---|---|
| Swagger | `springdoc-openapi-starter-webmvc-ui` | 사용 가능 |
| Local DB | `application-local.yaml`, H2 | 사용 가능 |
| Prod DB | `application-prod.yaml`, MySQL | 설정 확인, 실제 배포 검증 필요 |
| Docker | `Dockerfile`, `compose-prod.yaml` | 구성 확인 |
| CI | `.github/workflows/integration.yml` | main/dev push, PR 대상 빌드 |
| CD | `.github/workflows/deployment.yml` | main push 대상 Docker/EC2 배포 |

## 8. 테스트와 품질

### 기존 품질 리포트 기준

`docs/testing/quality-report-dev-2026-06-18.md` 기준:

| 항목 | 수치 |
|---|---:|
| 테스트 클래스 | 22 |
| 전체 테스트 | 82 |
| 성공 | 82 |
| 실패 | 0 |
| Line Coverage | 90.52% |
| Branch Coverage | 76.42% |

### 2026-06-20 최신 실행 기준

`.\gradlew.bat test jacocoTestReport` 실행 결과:

| 항목 | 결과 |
|---|---:|
| 실행 테스트 | 181 |
| 실패 | 2 |
| 실패 위치 | `UserControllerTest` |
| 실패 성격 | 로그인/토큰 재발급 응답 쿠키의 `Secure` 옵션 검증 실패 |

발표 자료에는 기존 리포트 수치와 최신 실행 결과를 섞어 쓰지 않는다. 최종 발표 전에는 테스트를 다시 통과시킨 뒤 최신 수치로 갱신해야 한다.

## 9. CI/CD와 배포

### CI

`.github/workflows/integration.yml`은 `main`, `dev` 브랜치 push와 pull request에서 실행된다.

주요 단계:

1. repository checkout
2. JDK 21 설정
3. Gradle cache 적용
4. secret yaml 생성
5. `./gradlew build`

### CD

`.github/workflows/deployment.yml`은 `main` 브랜치 push 시 실행된다.

주요 단계:

1. secret yaml 생성
2. JDK 21 설정
3. `./gradlew clean build -x test -Dspring.profiles.active=prod`
4. Docker Hub 로그인
5. Docker image build/push
6. `compose-prod.yaml` EC2 복사
7. EC2에서 `docker compose`로 backend 재기동

### 배포 링크

현재 문서 작성 시점에는 최종 배포 URL이 확인되지 않았다. 발표 전 반드시 Swagger URL과 실제 API base URL을 확인해야 한다.

## 10. 팀 구성 및 역할

팀원별 역할은 도메인 담당과 MVP 흐름 담당을 함께 표기한다. 최종 보고서에는 실제 Jira/PR 기준으로 한 번 더 검수한다.

| 이름 | 역할 | 담당 기능 | 발표 시 언급 포인트 |
|---|---|---|---|
| 남진우 | 팀장/총괄 | 일정, 문서, QA, 인증 사용자 주입 방식 정리 | MVP 범위 관리와 시연 흐름 |
| 이유진 | 백엔드 | 회원/Auth, 회원가입/로그인, 사용자 상태 | 사용자 진입과 인증 흐름 |
| 박재현 | 백엔드 | Talent, S3/재능 첨부파일 | 재능 등록/조회와 파일 고도화 |
| 이인희 | 백엔드 | Matching, MatchProposal, SWAP 방향 | 매칭 제안과 PURCHASE 진입 |
| 최윤서 | 백엔드 | Trade/Credit/Escrow, 정산 | 에스크로 보류와 구매 확정/정산 |

## 11. 수행 절차 및 방법

| 단계 | 수행 내용 | 산출물 |
|---|---|---|
| 기획 | 문제 정의, 타깃 유저, MVP 범위 확정 | 기획서 v2/v3 |
| 설계 | ERD, API 명세, 도메인 책임 분리 | ERD 문서, API 문서 |
| 구현 | Auth, Talent, Matching, Credit, Escrow, Trade 구현 | Spring Boot 코드 |
| 검증 | 단위/컨트롤러/서비스 테스트, 커버리지 확인 | JaCoCo, 테스트 리포트 |
| 배포 | GitHub Actions, Docker, EC2 배포 구성 | CI/CD workflow |
| 발표 준비 | 총괄 문서, 시연 스크립트, Canva 구성안 작성 | `docs/final` |

## 12. 발표 리스크

| 리스크 | 영향 | 대응 |
|---|---|---|
| 구매 확정 후 거래 재조회 상태 저장 이슈 | 완료 상태 시연 실패 가능 | `confirm` 이후 `GET /trade/{id}`가 `COMPLETED/RELEASED`인지 수정/재검증 |
| 최신 테스트/커버리지 미확정 | 품질 지표 신뢰도 하락 | 최종 테스트 실행 후 JaCoCo 수치 갱신 |
| 배포 URL 미확인 | 발표 시 실서비스 접근 불가 | 로컬 Swagger 시연 대체안 준비 |
| 팀 역할 자료 없음 | 보고서 완성도 저하 | 팀원별 담당 기능 수집 |
| UI 시연 범위 미확인 | 발표 흐름 단절 | API 시연을 기본 시연으로 두고 UI는 보조로 구성 |

## 13. 다른 채팅에서 받아야 할 자료

| 자료 | 필요 이유 | 담당 |
|---|---|---|
| 기획서 v2/v3 원문 | 서비스 서사와 최종 보고서 문장 보강 | 기획/문서 채팅 |
| 백엔드 최종 API 명세 | MVP 완성 여부 확정 | 백엔드 채팅 |
| 프론트 UI 화면/URL | UI 시연 흐름 작성 | 프론트 채팅 |
| 배포 URL/Swagger URL | 최종 시연 환경 확정 | 인프라 채팅 |
| 팀원별 역할 | 보고서 필수 항목 | 팀장 |
| 최종 테스트/커버리지 | 품질 지표 최신화 | QA/백엔드 |

## 14. 다음 작성 항목

1. 기획서 v2/v3 원문을 반영해 문제 정의와 차별화 문장 보강
2. 백엔드 최종 구현 상태를 반영해 MVP 흐름 표 갱신
3. 배포 링크와 Swagger 링크 입력
4. 팀원별 역할 표 완성
5. Canva 슬라이드별 핵심 문장으로 압축

## 15. 팀장 작업 관리

기준일: 2026-06-21
목표일: 2026-06-23 화요일

| 우선순위 | 해야 할 일 | 완료 기준 | 넘길 채팅/담당 |
|---|---|---|---|
| P0 | 각 기능에서 요청값으로 받는 사용자 식별값을 JWT 토큰 인증값에서 꺼내도록 수정 | 회원/재능/매칭/거래/크레딧 관련 API에서 `userId`, `memberId`, `buyerId`, `sellerId` 등을 클라이언트가 직접 넘기지 않아도 동작한다. | 백엔드, API / Swagger |
| P1 | 관리자 기능 개발 범위 확정 및 구현 | Swagger 또는 화면에서 최소 1개 이상의 관리자 흐름이 동작하고, 관리자 권한 체크 기준이 정리된다. | 백엔드, API / Swagger, QA / 테스트 |
| P1 | SWAP 거래 구현 범위 확정 | PURCHASE 단방향 거래 2건과 거래 그룹 구조, 멱등키 기준이 정리되고 최소 API 흐름이 동작한다. | 백엔드 / 설계 |
| P1 | 리뷰/신뢰 점수 구현 | 거래 완료 후 리뷰 작성/조회가 가능하고 중복 리뷰가 차단된다. | 백엔드 / QA |
| P1 | 채팅 시연 범위 검증 | REST/WebSocket 채팅 흐름 중 발표에 사용할 범위를 정하고 실제 호출 결과를 확보한다. | 백엔드 / API |

### 확인할 것

- 인증값 수정 대상 API 목록을 먼저 뽑는다.
- 관리자 기능은 최소 범위를 회원 목록 조회, 재능/거래 목록 조회, 상태 관리, 관리자 권한 체크 중 어디까지 포함할지 정한다.
- SWAP, 리뷰/신뢰 점수, 채팅은 추가 기능이라는 표현을 쓰더라도 최종 완성도 기준의 P1 범위로 관리한다.
- Swagger에서 사용자 식별값을 직접 받는 명세가 남아 있는지 확인한다.
- QA 시나리오에 일반 사용자와 관리자 권한 차이를 검증하는 케이스를 추가한다.

## 16. 작업 업데이트

기준일: 2026-06-22

### 완료된 일

| 우선순위 | 완료 항목 | 완료 기준 | 근거 |
|---|---|---|---|
| P0 | JWT 인증 사용자 주입 방식 통합 | 컨트롤러가 클라이언트 요청값 대신 인증된 사용자 ID를 사용한다. | PR #63 병합 완료 |
| P0 | `SecurityUser` / `@CurrentUser` 적용 | `SecurityContextHolder`의 인증 principal을 컨트롤러 단계에서 사용한다. | BATON-88 |
| P0 | 컨트롤러 테스트 인증 principal 정리 | 테스트에서 `@WithMockSecurityUser`로 인증 사용자를 주입한다. | BATON-88 |
| P0 | 회원가입 후 초기 크레딧 자동 지급 연결 | 회원가입 시 계좌 생성 로직이 연결되고 CI가 통과했다. | PR #62 병합 완료 |
| P0 | 진행 중 거래 재능 삭제 차단 | 진행 중 거래가 있는 재능 삭제 차단 로직과 테스트가 반영되고 CI가 통과했다. | PR #64 병합 완료 |

### 남은 일

| 우선순위 | 해야 할 일 | 완료 기준 | 넘길 채팅/담당 |
|---|---|---|---|
| P0 | 구매 확정 후 거래 재조회 상태 저장 이슈 해결 | confirm 응답과 trade detail 응답이 모두 `COMPLETED/RELEASED`다. | 백엔드 / QA |
| P1 | PURCHASE E2E 자동 테스트 추가 | 매칭 제안 수락부터 에스크로 보류, 제출, 구매 확정, 정산까지 한 흐름으로 검증된다. | 백엔드 / QA |
| P1 | 실패 케이스 테스트 | 크레딧 부족, 중복 수락, 권한 없음, 재확정, idempotency 재사용이 검증된다. | 백엔드 / QA |
| P1 | Swagger 사용자 식별값 명세 재점검 | BATON-88 병합 후 직접 입력 받는 `userId`, `buyerId`, `sellerId` 명세가 남아 있지 않다. | API / 문서 |

### 문서 정리 업데이트

기준일: 2026-06-22

| 우선순위 | 완료 항목 | 완료 기준 | 근거 |
|---|---|---|---|
| P0 | 최종 제출 문서 역할별 분리 | 팀원이 목적별로 필요한 문서를 바로 찾을 수 있다. | `docs/final/README.md` |
| P0 | 회의 문서 별도 관리 | 회의 안건/회의록이 최종 제출 문서와 섞이지 않는다. | `docs/meetings/README.md` |

| 우선순위 | 해야 할 일 | 완료 기준 | 넘길 채팅/담당 |
|---|---|---|---|
| P1 | API 명세 최신화 | CurrentUser 병합 이후 Swagger/API 문서에서 임시 userId 전달 설명이 제거된다. | API / 문서 |
| P1 | 시연 스크립트 최신화 | PURCHASE MVP 시연 순서가 최신 코드와 일치한다. | 백엔드 / 시연 |

### 백엔드 흐름 문서 업데이트

기준일: 2026-06-22

| 우선순위 | 완료 항목 | 완료 기준 | 근거 |
|---|---|---|---|
| P0 | MatchProposal 수락 흐름 문서화 | 매칭 수락 시 Trade 생성, Credit 예치, Escrow 생성, 상태 변경 흐름을 팀원이 확인할 수 있다. | `docs/final/architecture/match-proposal-accept-flow.md` |

| 우선순위 | 해야 할 일 | 완료 기준 | 넘길 채팅/담당 |
|---|---|---|---|
| P1 | MatchProposal 수락 동시성 테스트 | 중복 accept, accept/reject 경쟁 상황에서 Trade/Escrow/Credit이 중복 생성되지 않는다. | 백엔드 / 테스트 |
| P1 | MatchProposal lock 적용 여부 결정 | 수락/거절 경쟁을 DB lock 또는 상태 update 방식으로 방어할지 결정한다. | 백엔드 |

## 작업 업데이트 (2026-06-23, MVP 정상 흐름 테스트)

### 완료된 일

| 우선순위 | 완료 항목 | 완료 기준 | 근거 |
|---|---|---|---|
| P0 | MVP PURCHASE 정상 흐름 API 테스트 | 신규 회원가입부터 구매 확정/정산/CreditTransaction 조회까지 localhost API로 재현된다. | 2026-06-23 수동 API 테스트 완료 |
| P0 | 초기 크레딧 지급 검증 | 신규 구매자/제공자 모두 가입 직후 초기 크레딧 계좌와 WELCOME 내역이 생성된다. | balance 10000 확인 |
| P0 | 에스크로 보관/정산 검증 | 매칭 수락 후 구매자 크레딧이 escrowBalance로 이동하고, 구매 확정 후 제공자에게 정산된다. | confirm 응답 기준 Trade COMPLETED, Escrow RELEASED 확인 |
| P1 | MVP 테스트 상세 기록 문서 작성 | 요청/응답/생성 ID/잔액 변화가 `docs/testing`에 재현 가능하게 기록된다. | `docs/testing/mvp-purchase-flow-test-2026-06-23.md` |

### 남은 일

| 우선순위 | 해야 할 일 | 완료 기준 | 넘길 채팅/담당 |
|---|---|---|---|
| P0 | 구매 확정 후 거래 상세 재조회 상태 API 재검증 | `GET /api/v1/trade/{tradeId}` 재조회도 `COMPLETED/RELEASED`로 응답한다. | 백엔드 / QA |
| P1 | 실패 케이스 테스트 | 크레딧 부족, 중복 수락, 권한 없음, 재확정, idempotency 재사용이 검증된다. | 백엔드 / QA |
| P1 | 재능 상세 조회 인증 정책 정리 | 비로그인 조회 허용 여부를 결정하고 Swagger/API 문서와 구현이 일치한다. | 백엔드 / API |

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
| P0 | 서버 재기동 후 Swagger/Postman API 재검증 | 실제 API에서 confirm 직후 `GET /api/v1/trade/{tradeId}`가 `COMPLETED/RELEASED`로 응답 |
| P1 | 거래 취소 후 API 재조회 검증 | 통합 테스트는 완료. 서버 API에서 cancel 직후 `GET /api/v1/trade/{tradeId}`가 `CANCELLED/REFUNDED`로 응답하는지 확인 |
| P1 | PURCHASE E2E 자동 테스트 확장 | 회원가입부터 구매 확정/정산/이력 조회까지 자동화 |
