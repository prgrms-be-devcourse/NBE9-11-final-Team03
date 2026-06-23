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

## 주요 문서

| 문서 | 설명 |
| --- | --- |
| [프로젝트 기획서 v4.1](docs/final/overview/notion-project-plan-v4.md) | 서비스 방향, MVP 범위, 확장 계획 |
| [MVP 달성도 점검](docs/final/status/mvp-achievement-status-2026-06-22.md) | PURCHASE MVP 현재 상태 |
| [API 명세서](docs/api/api-spec-v1.md) | 주요 API 요약 |
| [시연 스크립트](docs/final/presentation/demo-script.md) | Swagger/Postman 시연 순서 |
| [시스템 구성도](docs/final/architecture/system-architecture.md) | 도메인/시스템 흐름 |
