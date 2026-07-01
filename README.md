# Baton

Baton은 주니어가 자신의 재능을 제공해 얻은 크레딧으로 다른 사용자의 재능을 구매할 수 있는 크레딧 기반 재능 거래 서비스입니다. MVP는 PURCHASE 거래와 에스크로 정산 흐름을 중심으로 검증했습니다.

## 실행

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

- Local Swagger: `http://localhost:8080/swagger-ui/index.html`
- Local OpenAPI: `http://localhost:8080/v3/api-docs`
- 배포 서비스: [https://baton.io.kr](https://baton.io.kr)
- 백엔드 직접 접근: `http://54.116.23.255`
- 배포 Swagger: `http://54.116.23.255/swagger-ui/index.html`

## 최종 제출 필수 문서

아래 문서는 고용노동부 지정 결과보고서 PPT와 최종 발표자료의 필수 항목을 작성하기 위한 기준 문서입니다.

| 문서 | 역할 |
| --- | --- |
| [프로젝트 총괄](docs/final/overview/project-master.md) | 개요, 역할, 수행 절차, 수행 경과 |
| [최종 API 명세](docs/api/api-spec-v1.md) | PURCHASE 중심 API와 부가 API |
| [시스템 구성도](docs/final/architecture/system-architecture.md) | 애플리케이션, 도메인, CI/CD 구조 |
| [발표 구성안](docs/final/presentation/presentation-outline.md) | 20분 발표용 슬라이드 원고 |
| [API/UI 시연 스크립트](docs/final/presentation/demo-script.md) | 필수 시연 영상 녹화 순서 |
| [최신 품질 리포트](docs/testing/quality-report-2026-07-01.md) | 최신 DEV CI와 커버리지 기준 |
| [서버 성능 테스트 리포트](docs/testing/load-test-report-2026-06-29.md) | k6 성능 검증 결과 |
| [자체 평가 및 회고](docs/final/retrospective/self-evaluation-retrospective.md) | 성과, 한계, 개선 계획 |

최종 제출 시에는 위 문서를 바탕으로 작성한 지정 양식 PPT, API 시연 영상, UI 시연 영상, 배포 링크를 Notion 팀 페이지와 Google Form에 제출합니다.
