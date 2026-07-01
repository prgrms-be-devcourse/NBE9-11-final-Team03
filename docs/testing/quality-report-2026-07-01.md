# Baton 최종 테스트 및 커버리지 리포트

> 기준일: 2026-07-01
> 최신 DEV: `bc39d192b0acc6697ba2be0913beaffa905fcf77`
> 실제 배포 main: `4f183829a3a284af8bee0b58e8778283a1246e98`

## 1. 최종 판정

| 항목 | 결과 |
| --- | --- |
| 최신 DEV CI | 성공 |
| CI 실행 | [GitHub Actions](https://github.com/prgrms-be-devcourse/NBE9-11-final-Team03/actions/runs/28463849327) |
| 실제 배포 main CI | 성공 |
| 실제 배포 main CD | [성공](https://github.com/prgrms-be-devcourse/NBE9-11-final-Team03/actions/runs/28427109836) |
| 배포 API/Swagger/OpenAPI | HTTP 200 |
| 최신 가용 Line Coverage | 87.47% |
| 최신 가용 Branch Coverage | 74.55% |

최신 DEV의 `integration.yml`은 `./gradlew build`를 실행하며 해당 실행이 성공했다. 따라서 최종 제출에서는 DEV CI 성공을 최신 테스트 판정으로 사용한다.

## 2. 정렬 테스트 해결 이력

최신 DEV 반영 전 작업 브랜치에서 전체 483개 테스트를 실행했을 때 `MatchProposalRepositoryTest` 2건이 실패했다.

- 현재 로그인한 제공자가 받은 매칭 제안 목록 조회
- 현재 로그인한 요청자가 보낸 매칭 제안 목록 조회

동일한 `createdAt`을 가진 데이터의 순서가 DB 실행마다 달라지는 것이 원인이었다. 최신 DEV 커밋 `ae1049b`에서 다음과 같이 안정적인 보조 정렬을 추가했다.

```sql
order by mp.createdAt desc, mp.id desc
```

이 변경이 포함된 최신 DEV `bc39d192`의 CI가 성공했으므로 해결 완료로 판정한다.

## 3. JaCoCo 결과

아래 수치는 2026-06-29 최신 가용 전체 리포트 기준이다.

| 지표 | 커버 | 전체 | 비율 |
| --- | ---: | ---: | ---: |
| Line | 2,129 | 2,434 | 87.47% |
| Branch | 334 | 448 | 74.55% |
| Instruction | 10,302 | 11,719 | 87.91% |
| Method | 528 | 605 | 87.27% |
| Class | 174 | 186 | 93.55% |

최종 PPT에는 기준일을 함께 표시한다. 최신 DEV 기준 JaCoCo를 다시 생성하면 해당 수치로 교체한다.

## 4. 최신 DEV 변경 사항

| 변경 | 커밋 | 상태 |
| --- | --- | --- |
| MatchProposal 안정 정렬 | `ae1049b` | main 배포 반영 |
| 비활성 재능 목록/검색 제외 | `7370e64` | main 배포 반영 |
| 재능 GET 비로그인 허용 | `c3260f9` | main 배포 반영 |
| 이메일 인증 재전송 제한 | `f60a579` | main 배포 반영 |
| SMTP 운영 환경 변수 | `00e4615` | DEV, main 미반영 |
| 재배포 시 프론트 컨테이너 유지 | `2a8a091` | DEV, main 미반영 |
| 재능 목록 조회수 부작용 제거/작성자 정보 추가 | `c0b8858` | DEV, main 미반영 |
| SMTP 포트 환경 변수명 수정 | `9c8a2a6` | DEV, main 미반영 |
| IN_PROGRESS 거래 취소 금지 | `bc39d192` | DEV, main 미반영 |

## 5. 발표 사용 기준

- 최신 테스트 상태는 `DEV CI 성공`으로 표기한다.
- 커버리지는 `2026-06-29 최신 가용 전체 리포트`라고 표기한다.
- 실제 배포 SHA는 `4f18382`, 최신 DEV SHA는 `bc39d192`로 구분한다.
- SMTP/CD 보완은 main 재배포 전까지 실제 배포 완료라고 말하지 않는다.
- 재능 목록 응답 보완도 PR #122 병합 전까지 실제 배포 완료라고 말하지 않는다.
