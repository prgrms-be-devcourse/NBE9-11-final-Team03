# Baton

크레딧 기반 재능 거래/교환 서비스입니다. MVP는 회원가입부터 재능 등록, 매칭 제안, PURCHASE 거래 생성, 에스크로 보류, 결과물 제출, 구매 확정, 제공자 정산까지 이어지는 단방향 거래 흐름을 중심으로 합니다.

## 실행

```bash
./gradlew bootRun
```

Windows:

```bash
./gradlew.bat bootRun
```

Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

## 문서 인덱스

최종 제출/발표 기준으로는 `최종 제출 핵심 문서`를 먼저 확인합니다. 회의록, 리뷰, 템플릿은 보조 문서로 관리합니다.

### 최종 제출 핵심 문서

| 문서 | 설명 |
| --- | --- |
| [최종 문서 안내](docs/final/README.md) | 최종 제출 문서 구조와 우선 확인 문서 |
| [프로젝트 총괄 문서](docs/final/overview/project-master.md) | 프로젝트 개요, 역할, 수행 절차, 리스크 |
| [프로젝트 기획서 v4.2](docs/final/overview/notion-project-plan-v4.md) | 서비스 방향, MVP 범위, 확장 계획 |
| [최종 제출물 갭 체크리스트](docs/final/status/final-submission-gap-checklist.md) | 제출 항목, 남은 일, 완료 기준 |
| [MVP 달성도 점검](docs/final/status/mvp-achievement-status-2026-06-22.md) | PURCHASE MVP 현재 상태 |
| [MVP 이후 필수 구현 범위 메모](docs/final/status/post-mvp-required-scope-note.md) | SWAP, 관리자, 리뷰, 채팅의 P1 관리 기준 |
| [멘토링 사전 브리프](docs/final/status/mentoring-brief-2026-06-23.md) | 멘토링 전 공유용 프로젝트 흐름과 질문 요약 |
| [MVP 실제 요청/응답 기록](docs/final/status/mvp-api-test-request-response-log.md) | Swagger/Postman 재현용 실제 응답 |
| [API 명세서](docs/api/api-spec-v1.md) | 주요 API 요약 |
| [시연 스크립트](docs/final/presentation/demo-script.md) | Swagger/Postman 시연 순서 |
| [시스템 구성도](docs/final/architecture/system-architecture.md) | 도메인/시스템 흐름 |
| [자체 평가 및 회고](docs/final/retrospective/self-evaluation-retrospective.md) | 팀 회고와 개선 계획 |

### 설계/검증 문서

| 문서 | 설명 |
| --- | --- |
| [ERD v2 검수 문서](docs/erd/erd-v2-review.md) | 테이블 역할, 관계, 구현 반영 상태 |
| [MatchProposal 수락 흐름](docs/final/architecture/match-proposal-accept-flow.md) | 매칭 수락 후 Trade/Credit/Escrow 연결 흐름 |
| [MVP PURCHASE 테스트 기록](docs/testing/mvp-purchase-flow-test-2026-06-23.md) | 정상 흐름 수동 API 테스트 결과 |
| [품질 리포트 2026-06-18](docs/testing/quality-report-dev-2026-06-18.md) | 과거 테스트/커버리지 결과 |

### 발표/고도화 문서

| 문서 | 설명 |
| --- | --- |
| [발표 구성안](docs/final/presentation/presentation-outline.md) | 발표 슬라이드 흐름 초안 |
| [고려 기술 및 고도화 양식](docs/final/templates/technology-and-enhancement-template.md) | 추가 기능과 기술 선택 근거 정리 |
| [트러블슈팅 양식](docs/final/templates/troubleshooting-template.md) | 문제 재현/원인/해결 기록 |
| [트레이드오프 양식](docs/final/templates/trade-off-template.md) | 기술 선택과 대안 비교 기록 |

### 회의/협업 기록

| 문서 | 설명 |
| --- | --- |
| [회의 문서 안내](docs/meetings/README.md) | 회의 문서 관리 기준 |
| [11차 회의 안건](docs/meetings/meeting-agenda-2026-06-21.md) | 2026-06-21 회의 안건 |
| [11차 회의록](docs/meetings/meeting-minutes-2026-06-21.md) | 2026-06-21 회의 결과 |
| [MVP 싱크 회의 문서](docs/meetings/meeting-2026-06-21-mvp-sync.md) | MVP 진행 상황과 작업 분배 |

### 보조 템플릿/리뷰

| 문서 | 설명 |
| --- | --- |
| [품질 리포트 템플릿](docs/testing/quality-report-template.md) | 테스트/커버리지 리포트 작성 양식 |
| [리팩터링 기록 템플릿](docs/refactoring/refactoring-history-template.md) | 리팩터링 내역 작성 양식 |
| [PR #54 리뷰 기록](docs/reviews/pr-54-match-proposal-accept-flow-review.md) | 매칭 수락 흐름 PR 리뷰 기록 |
