# Baton 최종 시연 스크립트

> 문서 버전: v2.0
> 기준일: 2026-07-01
> 공식 제출: API 시연 영상과 UI 시연 영상 모두 필요

## 1. 공통 준비

| 항목 | 값/준비 |
| --- | --- |
| 배포 Swagger | `http://54.116.23.255/swagger-ui/index.html` |
| 로컬 대체 Swagger | `http://localhost:8080/swagger-ui/index.html` |
| 사용자 | 신규 구매자/제공자 2명 |
| 초기 크레딧 | 각 10,000 |
| 기록할 ID | user, talent, proposal, trade, submission |
| 영상 | API와 UI를 별도 녹화하고 링크 권한 확인 |

## 2. API 시연 영상

| 순서 | 호출 | 확인 포인트 |
| ---: | --- | --- |
| 1 | `POST /api/v1/auth/signup` | 구매자/제공자 생성 |
| 2 | `POST /api/v1/auth/login` | JWT 발급 |
| 3 | `GET /api/v1/credit/balance` | balance 10,000, escrow 0 |
| 4 | `POST /api/v1/talents` | 제공자 재능 생성 |
| 5 | `GET /api/v1/talents/{id}` | 재능 가격/제공자 확인 |
| 6 | `POST /api/v1/match-proposals` | REQUESTED |
| 7 | `PATCH /api/v1/match-proposals/{id}/accept` | ACCEPTED, Idempotency-Key 사용 |
| 8 | `GET /api/v1/trade/{id}` | IN_PROGRESS, HELD |
| 9 | `GET /api/v1/credit/balance` | 구매자 balance 감소, escrow 증가 |
| 10 | `POST /api/v1/trade/{id}/submission` | UNDER_REVIEW |
| 11 | `PATCH /api/v1/trade/{id}/confirm` | COMPLETED, RELEASED |
| 12 | `GET /api/v1/trade/{id}` | 완료 상태 DB 재조회 확인 |
| 13 | `GET /api/v1/credit/transactions` | WELCOME/HOLD/RELEASE 원장 확인 |

API 영상에서는 매칭 수락 응답에서 다음 단계의 `tradeId`를 얻는 방법을 사전에 확정한다. 응답에 없다면 준비된 테스트 데이터의 ID를 사용하고 영상 자막으로 설명한다.

최신 DEV에서는 카테고리 및 재능 목록/검색/상세 GET API를 비로그인 사용자에게 허용한다. 영상에서는 로그인 전 재능 탐색과 로그인 후 제안 생성의 차이를 보여줄 수 있다.

최신 DEV의 재능 목록/검색 응답에는 작성자 ID와 닉네임이 포함되며 목록 조회 자체로 조회수가 증가하지 않는다. PR #122 배포 전에는 이 필드를 실제 배포 영상에서 기대하지 않는다.

최신 DEV에서는 `IN_PROGRESS` 거래 취소 요청이 `TRADE-400-009`로 거절된다. 정상 PURCHASE 영상에서는 거래 취소를 호출하지 않는다.

## 3. UI 시연 영상

1. 구매자 회원가입/로그인
2. 초기 크레딧 확인
3. 재능 목록/검색/상세 조회
4. 매칭 제안 생성
5. 제공자 계정으로 전환해 제안 수락
6. 거래 상세와 에스크로 상태 확인
7. 제공자 결과물 제출
8. 구매자가 결과물을 확인하고 구매 확정
9. 제공자의 정산 잔액과 양측 크레딧 내역 확인

UI에서 구현되지 않은 단계는 API 영상으로 대체하지 말고, UI 영상의 미구현 범위를 명확히 표시한다.

## 4. 실패 대비

| 상황 | 대응 |
| --- | --- |
| 배포 서버 불안정 | 로컬 Swagger 녹화본 사용 |
| S3 업로드 실패 | 사전 업로드한 fileKey로 제출 흐름 시연 |
| 테스트 데이터 충돌 | 영상용 이메일/닉네임에 타임스탬프 사용 |
| tradeId 확인 불가 | 사전 생성 데이터와 ID 기록표 사용 |
| 실시간 시연 실패 | 제출한 녹화 영상을 재생 |

## 5. 녹화 완료 체크

- [ ] API 흐름이 회원가입부터 정산까지 끊기지 않는다.
- [ ] UI 핵심 흐름이 실제 구현 범위와 일치한다.
- [ ] 토큰, 비밀번호, 민감한 환경 값이 영상에 노출되지 않는다.
- [ ] 영상 링크를 로그인하지 않은 상태에서 열 수 있다.
- [ ] PPT와 Notion에 같은 링크를 넣었다.
