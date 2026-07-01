# Baton 프로젝트 총괄 문서

> 문서 버전: v2.0
> 기준일: 2026-07-01
> 프로젝트: Baton
> 팀: Team3
> 제출 기준: 고용노동부 지정 결과보고서 PPT 필수 항목

## 1. 프로젝트 개요

Baton은 현금 부담 때문에 필요한 도움을 얻기 어려운 주니어가 자신의 재능을 기반으로 다른 사용자의 재능을 구매할 수 있도록 돕는 크레딧 기반 재능 거래 서비스다. 사용자는 재능을 등록하고 매칭 제안을 주고받으며, PURCHASE 거래가 시작되면 구매자의 크레딧은 에스크로에 보관된다. 구매 확정 후 제공자에게 정산해 일방 이탈과 미이행 위험을 낮춘다.

장기적으로 SWAP까지 확장하지만 MVP는 PURCHASE 정상 흐름의 안정화에 집중한다.

## 2. 문제와 해결 방식

| 문제 | Baton의 해결 방식 |
| --- | --- |
| 주니어의 현금 부담 | 내부 크레딧으로 거래 진입 비용 완화 |
| 개인 거래의 신뢰 부족 | 거래 중 크레딧을 에스크로에 보관 |
| 포트폴리오가 부족한 제공자의 진입 장벽 | 작은 재능부터 등록하고 거래 이력 축적 |
| 복잡한 양방향 교환 정책 | PURCHASE를 먼저 완성하고 SWAP은 후속 확장 |

## 3. MVP 성공 흐름

1. 회원가입과 로그인
2. 초기 크레딧 지급
3. 재능 등록과 조회
4. 매칭 제안 생성과 수락
5. PURCHASE 거래 생성
6. 구매자 크레딧 차감과 에스크로 보관
7. 거래 상태 및 결과물 조회
8. 결과물 제출과 구매 확정
9. 제공자 크레딧 정산
10. 크레딧 변동 내역 기록과 조회

위 흐름은 로컬 HTTP API와 배포 서버 대상 k6 purchase smoke 시나리오에서 재현했다.

## 4. 기능 상태

| 우선순위 | 기능 | 상태 | 발표 기준 |
| --- | --- | --- | --- |
| P0 | Auth, Talent, Matching, Trade, Credit, Escrow | 구현 완료 | PURCHASE 메인 흐름으로 설명 |
| P0 | 결과물 제출/구매 확정/정산 | 구현 완료 | 시연 영상 재검증 후 사용 |
| P0 | CreditTransaction 조회 | 구현 완료 | 정산 근거로 사용 |
| P1 | S3 재능/결과물 첨부 | 구현, 최종 시연 검증 필요 | 검증된 범위만 사용 |
| P1 | 채팅 REST/WebSocket | 구현, 최종 시연 검증 필요 | 보조 기능으로 분리 |
| P1 | 관리자, SWAP, 리뷰/신뢰 점수 | 구현 범위 최종 확인 필요 | 완료 근거 없으면 확장 계획 |
| P2 | 알림, 유료 충전, 추천 고도화 | 후순위 | 로드맵으로만 설명 |

## 5. 기술 스택

| 영역 | 기술 | 선택 이유 |
| --- | --- | --- |
| Backend | Spring Boot, Java 21 | REST API와 트랜잭션 기반 도메인 구현 |
| Persistence | JPA, QueryDSL | 엔티티 중심 개발과 동적 조회 |
| Database | H2, MySQL | 테스트/로컬과 배포 환경 분리 |
| Security | Spring Security, JWT | 토큰 기반 인증과 사용자 주입 |
| Test | JUnit, JaCoCo, k6 | 기능, 커버리지, 성능 검증 |
| API Docs | Swagger/OpenAPI | API 명세와 시연 |
| Delivery | GitHub Actions, Docker, EC2 | 자동 빌드와 컨테이너 배포 |
| Storage | AWS S3 | 첨부파일과 결과물 저장 |

## 6. 팀 구성 및 역할

기존 역할 기준은 아래와 같으며, 제출 전 팀원별 실명·대표 Jira/PR을 최종 확인한다.

| 역할 | 담당 영역 | 발표 근거 |
| --- | --- | --- |
| 팀장/총괄 | 일정, 문서, QA, 인증 사용자 주입 정리 | 범위 관리와 시연 흐름 |
| 백엔드 | Auth/User | 회원가입, 로그인, 사용자 상태 |
| 백엔드 | Talent/S3 | 재능 CRUD와 첨부파일 |
| 백엔드 | Matching/Chat | 매칭 제안과 실시간 소통 |
| 백엔드 | Trade/Credit/Escrow | 거래 상태, 예치, 환불, 정산 |

## 7. 수행 절차 및 방법

| 단계 | 수행 내용 | 산출물 |
| --- | --- | --- |
| 기획 | 문제 정의, 타깃, PURCHASE 우선순위 결정 | 기획서 v2~v4 |
| 설계 | 요구사항, WBS, ERD, API와 도메인 책임 분리 | ERD/API 문서 |
| 개발 | 도메인별 기능 구현과 PR 리뷰 | Spring Boot 코드/Jira/PR |
| 검증 | 단위·통합·HTTP API·k6 테스트 | 테스트/커버리지/부하 리포트 |
| 배포 | Actions, Docker Hub, EC2 구성 | CI/CD 로그와 배포 링크 |
| 발표 | PPT, API/UI 시연 영상, 회고 | 최종 제출 자료 |

## 8. 수행 경과

- PURCHASE 범위를 우선 확정하고 인증 사용자 주입 방식을 `@CurrentUser`로 통일했다.
- 회원가입 직후 초기 크레딧 지급과 거래 원장 조회를 연결했다.
- 매칭 수락 시 Trade/Credit/Escrow가 함께 생성되도록 구현했다.
- 구매 확정 후 DB 상태가 남지 않던 문제를 flush 순서 수정과 통합 테스트로 해결했다.
- 2026-06-29 배포 서버에서 k6 4개 시나리오를 수행해 실패율 0%와 설정 임계치 통과를 확인했다.
- 작업 브랜치 전체 테스트에서 발생한 MatchProposal 정렬 실패 2건은 최신 DEV에서 `id desc` 보조 정렬을 추가해 해결했고, DEV CI가 성공했다.
- `4f18382` main 배포는 CI/CD 성공 후 서버, Swagger, OpenAPI HTTP 200을 확인했다.
- `bc39d192` DEV에는 SMTP/CD 보완, 재능 목록 조회 부작용 제거, 작성자 정보 응답, IN_PROGRESS 거래 취소 금지가 반영됐으며 main 병합 전이다.

## 9. 테스트와 성능

| 항목 | 최신 결과 |
| --- | --- |
| 최신 DEV CI | `bc39d192`, 성공 |
| 정렬 회귀 | `createdAt desc, id desc`로 안정화 |
| Line Coverage | 87.47%, 2026-06-29 최신 가용 리포트 |
| Branch Coverage | 74.55%, 2026-06-29 최신 가용 리포트 |
| k6 | smoke/read/login/purchase 모두 실패율 0%, 임계치 통과 |

## 10. CI/CD와 배포

- CI: `.github/workflows/integration.yml`
- CD: `.github/workflows/deployment.yml`
- 최신 DEV: `bc39d192`, [CI 성공](https://github.com/prgrms-be-devcourse/NBE9-11-final-Team03/actions/runs/28463849327)
- 실제 배포 main: `4f18382`, [CD 성공](https://github.com/prgrms-be-devcourse/NBE9-11-final-Team03/actions/runs/28427109836)
- 배포 API: `http://54.116.23.255` (HTTP 200)
- 배포 Swagger: `http://54.116.23.255/swagger-ui/index.html` (HTTP 200)
- 배포 OpenAPI: `http://54.116.23.255/v3/api-docs` (HTTP 200)
- DEV의 SMTP/CD/재능 목록 보완은 [PR #122](https://github.com/prgrms-be-devcourse/NBE9-11-final-Team03/pull/122) 병합 전이라 실제 배포에는 아직 미반영

## 11. 제출 리스크

| 우선순위 | 리스크 | 조치 |
| --- | --- | --- |
| P0 | API/UI 시연 영상 링크 없음 | 녹화 및 공개 권한 확인 |
| P0 | 지정 PPT 미완성 | 공식 항목 누락 여부 검수 |
| P0 | Notion/Google Form 이중 제출 | 제출 담당자와 완료 증빙 확인 |
| P1 | 팀원별 역할 근거 부족 | Jira/PR 링크 추가 |
| P1 | DEV와 실제 배포 SHA 차이 | PR #122 병합/재배포 여부 확인 |
