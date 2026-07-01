# PR #54 리뷰 기록 - 매칭 제안 수락 흐름

## 요약

- PR: https://github.com/prgrms-be-devcourse/NBE9-11-final-Team03/pull/54
- Jira: BATON-79
- 제목: 매칭 제안 수락 거래 흐름 연결
- 상태 확인일: 2026-06-22
- 현재 판단: 병합을 막을 P0 필수 수정은 없음.

PR #54는 매칭 제안 수락 시 `Trade` 생성, 구매자 크레딧 에스크로 보류, `Escrow` 생성, 매칭 제안 `ACCEPTED` 전환 흐름을 연결한다.
GitHub Actions build는 성공했고, PR은 `mergeable: clean` 상태로 확인했다.

## 현재 결정

현재 기준으로 즉시 머지를 막아야 하는 P0 이슈는 보이지 않는다.

기존에 우려했던 `Idempotency-Key` 재사용 문제는 작성자가 일부 반영했다.
현재 구현은 `MATCH-PROPOSAL-ACCEPT-{proposalId}:{clientKey}` 형태의 서버 prefix를 붙여 `CreditService.holdForEscrow`에 전달한다.
따라서 다른 매칭 제안에서 같은 클라이언트 키를 재사용해도 최종 idempotency key가 제안별로 달라져, 크레딧 보류 없이 다른 제안이 수락되는 위험은 완화되었다.

또한 `Trade.match_id`에 unique 제약이 있어, 같은 매칭 제안에 대해 거래가 조용히 2개 커밋되는 최악의 상황은 DB 레벨에서 막힌다.

## P0

없음.

## P1

### 1. 0 크레딧 재능 정책 불일치

현재 `TalentCreateReq`는 `creditPrice`를 `@PositiveOrZero`로 허용한다.
즉 API 기준으로 `creditPrice = 0`인 재능 등록이 가능하다.

하지만 PR #54의 수락 흐름은 항상 `creditService.holdForEscrow(...)`를 호출한다.
`CreditService.holdForEscrow`는 `amount <= 0`이면 `INVALID_CREDIT_AMOUNT` 예외를 던진다.

따라서 0 크레딧 재능이 등록된 경우, 매칭 제안 수락 시 실패할 수 있다.

리뷰 코멘트 예시:

```text
현재 TalentCreateReq는 creditPrice=0을 허용하지만, 수락 흐름에서는 creditService.holdForEscrow(...)를 항상 호출하고 있습니다.
CreditService.holdForEscrow는 amount <= 0이면 INVALID_CREDIT_AMOUNT를 던지므로, 0 크레딧 재능 제안 수락이 실패할 수 있습니다.

정책상 0 크레딧 재능을 금지할 거라면 TalentCreateReq/TalentUpdateReq 검증을 @Positive로 바꾸고,
허용할 거라면 creditPrice > 0일 때만 holdForEscrow를 호출하도록 가드가 필요해 보입니다.
```

권장 판단:

- MVP에서 무료 재능을 허용하지 않을 거면 `creditPrice` 검증을 `@Positive`로 변경한다.
- 무료 재능을 허용할 거면 `creditPrice > 0`일 때만 크레딧 보류를 수행한다.

### 2. 원본 Idempotency-Key blank 검증

현재 서비스에서 서버 prefix를 붙인 뒤 `CreditService.holdForEscrow`에 전달한다.
예를 들어 클라이언트가 빈 문자열을 보내도 최종 키는 `MATCH-PROPOSAL-ACCEPT-1:`처럼 non-blank가 된다.

헤더를 필수 계약으로 유지하려면, prefix를 붙이기 전에 원본 `idempotencyKey`가 null 또는 blank인지 먼저 검증하는 편이 더 명확하다.

리뷰 코멘트 예시:

```text
현재 서버 prefix를 붙인 뒤 CreditService로 넘기기 때문에, 원본 Idempotency-Key가 blank여도 최종 키는 blank가 아니게 됩니다.
컨트롤러/서비스 초입에서 원본 idempotencyKey가 null/blank인지 먼저 검증하면 API 계약이 더 명확해질 것 같습니다.
```

### 3. 동시 수락 요청 처리

같은 매칭 제안에 대한 동시 수락 요청이 들어오면 `Trade.match_id` unique 제약으로 중복 거래 커밋은 막힌다.
다만 한쪽 요청이 DB constraint 예외로 떨어질 수 있다.

MVP에서는 치명적이지 않지만, 후속으로는 다음 중 하나를 고려할 수 있다.

- `MatchProposal` 조회 시 pessimistic lock 적용
- `Trade.match_id` 중복 예외를 도메인 예외로 변환
- 이미 생성된 거래/에스크로를 조회해 멱등 응답 반환

## P2

### 1. providerId query parameter 제거 예정

PR #54의 `MatchProposalController.acceptMatchProposal`은 아직 `providerId`를 query parameter로 받는다.
이 부분은 BATON-88 작업에서 `@CurrentUser SecurityUser` 기반으로 정리할 예정이다.

PR #54 머지 후 BATON-88 브랜치를 rebase할 때 충돌과 동작 정합성을 확인해야 한다.

### 2. SWAP 확장

현재 PR은 `TradeType.SWAP`으로만 구분하고 실제 양방향 거래 그룹 구조는 후속 작업으로 남긴다.
PR 설명과 구현 범위가 일치하므로 이번 PR에서 막을 내용은 아니다.

## 권장 리뷰 결론

팀장 관점에서는 아래처럼 판단할 수 있다.

```text
P0 필수 수정은 없어 보여서 머지 자체를 막을 정도는 아닙니다.
다만 creditPrice=0 정책은 현재 DTO 검증과 수락 흐름이 충돌할 수 있으니 P1 보강 의견으로 남기는 게 좋겠습니다.
providerId 인증 기반 전환은 BATON-88에서 이어서 정리하면 됩니다.
```

## 후속 작업 체크리스트

- [ ] 0 크레딧 재능 허용 여부 결정
- [ ] 허용하지 않으면 `creditPrice` 검증을 `@Positive`로 변경
- [ ] 허용하면 수락 흐름에서 `creditPrice > 0`일 때만 escrow hold 수행
- [ ] 원본 `Idempotency-Key` blank 검증 추가 여부 결정
- [ ] PR #54 머지 후 BATON-88에서 `providerId` query parameter 제거 충돌 확인
