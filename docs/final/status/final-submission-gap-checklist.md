# Baton 최종 제출 현황

> 문서 버전: v2.0
> 기준일: 2026-07-01
> 제출 마감: 2026-07-01 자정
> 제출처: Notion 프로젝트 팀 페이지 및 대시보드, Google Form
> 공식 근거: Notion `프로젝트 제출 및 발표`

## 1. 공식 제출물

| 제출물 | 공식 요구 | 현재 대응 | 상태 |
| --- | --- | --- | --- |
| 지정 결과보고서 PPT | 고용노동부 양식의 모든 항목 포함 | Markdown 원고 준비 | 검증 필요 |
| 발표자료 | `[발표자료] 03팀_최종 팀프로젝트` | `presentation-outline.md` | 검증 필요 |
| API 시연 영상 | Postman/Swagger UI 호출 영상 | `demo-script.md` | 검증 필요 |
| UI 시연 영상 | 구현 UI 기반 영상 | 저장소 외부 자료 필요 | 검증 필요 |
| 배포 링크 | 실행 가능한 서비스 링크 | `http://54.116.23.255` | HTTP 200 확인 |
| GitHub 저장소 | 프로그래머스 Organization 저장소 | `NBE9-11-final-Team03` | 구현 완료 |
| Notion 제출 | 최종 결과물 페이지 및 대시보드 링크 | 팀 페이지 확인 필요 | 검증 필요 |
| Google Form 제출 | 최종 결과물 제출 | 제출자/링크 확인 필요 | 검증 필요 |

## 2. 발표자료 필수 항목

| 항목 | 근거 문서 | 상태 |
| --- | --- | --- |
| 프로젝트 개요 | `project-master.md`, `notion-project-plan-v4.md` | 구현 완료 |
| 팀 구성 및 역할 | `project-master.md` | 팀원 최종 확인 필요 |
| 수행 절차 및 방법 | `project-master.md` | 구현 완료 |
| 수행 경과 | `project-master.md`, Jira/PR | 링크 보강 필요 |
| JUnit/JaCoCo | `quality-report-2026-07-01.md` | DEV CI 성공, 커버리지 재생성 필요 |
| 최종 API 명세 | `api-spec-v1.md`, Swagger | 배포 Swagger/OpenAPI HTTP 200 확인 |
| CI/CD 로그 | DEV CI `28463849327`, main CD `28427109836` | 성공, PPT 캡처 필요 |
| 서버 성능 테스트 | `load-test-report-2026-06-29.md` | 구현 완료 |
| API 시연 영상 | `demo-script.md` | 녹화/링크 필요 |
| UI 시연 영상 | 프론트 결과물 | 녹화/링크 필요 |
| 자체 평가 및 회고 | `self-evaluation-retrospective.md` | 팀원 의견 보강 필요 |

## 3. 최신 검증 결과

| 검증 | 결과 | 판정 |
| --- | --- | --- |
| PURCHASE 수동 API 흐름 | 회원가입부터 정산/원장 조회까지 재현 | 통과 |
| 구매 확정 상태 영속화 | `COMPLETED/RELEASED` 재조회 확인 | 통과 |
| k6 smoke/read/login/purchase | 전 시나리오 실패율 0%, 임계치 통과 | 통과 |
| 최신 DEV CI | `bc39d192` | 성공 |
| MatchProposal 정렬 | `createdAt desc, id desc` 적용 | 해결 |
| JaCoCo 최신 생성본 | 2026-06-29 Line 87.47%, Branch 74.55% | 참고 가능 |
| 실제 배포 main | `4f18382` | CI/CD 성공 |
| 배포 URL | `http://54.116.23.255` | HTTP 200 |
| 배포 Swagger/OpenAPI | Swagger, `/v3/api-docs` | HTTP 200 |

## 4. P0 마감 전 조치

- [ ] JaCoCo 리포트 재생성 및 PPT 수치 확정
- [ ] GitHub Actions CI/CD 성공 로그 캡처
- [ ] DEV→main PR #122 병합 및 재배포 여부 확인
- [ ] API 시연 영상 녹화 및 링크 권한 확인
- [ ] UI 시연 영상 녹화 및 링크 권한 확인
- [ ] 지정 PPT 작성 및 PDF 제출본 검수
- [ ] 팀원별 역할/기여 내용 최종 확인
- [ ] Notion과 Google Form 양쪽 제출

## 5. 팀장/팀원/다른 채팅 구분

### 팀장 확인

- 제출자, Google Form 링크, Notion 최종 페이지
- PPT 파일명과 지정 양식 누락 여부
- 배포/Swagger/영상 링크 접근 권한
- 발표에서 구현 완료라고 말할 기능 범위

### 팀원에게 맡길 것

- 담당 기능과 대표 Jira/PR 한 줄 요약
- CI/CD 성공 로그 및 배포 화면
- UI/API 시연 영상 원본
- 개인 회고 한 줄

### 다른 채팅에서 받을 자료

- 프론트 최종 URL과 UI 시연 동선
- 인프라 최종 배포 상태와 Actions 링크
- 테스트 실패 해결 결과와 재생성 커버리지
