# Baton ERD v2 검수 문서

기준 브랜치: `dev`
> 문서 버전: v2.2
> 기준일: 2026-06-23
> 기준 브랜치: `dev`
> 문서 상태: 최신 구현/테스트 기준 반영
> 원본 기준일: 2026-06-18

## 변경 이력

| 버전 | 날짜 | 변경 내용 | 상태 |
| --- | --- | --- | --- |
| v2.0 | 2026-06-18 | ERD v2 검토 문서 작성 | 작성 완료 |
| v2.1 | 2026-06-22 | PURCHASE 구현 상태 변화에 따른 검토 메모 추가 | 구현 반영 필요 |
| v2.2 | 2026-06-23 | Trade, TradeSubmission, CreditTransaction, 초기 크레딧 자동 생성 상태 반영 | 최신 테스트 기준 |
목적: ERD v2를 제출 전 빠르게 검수할 수 있도록, 테이블 역할과 관계를 구현 기준으로 정리한다.

## 1. ERD v2 한 줄 요약

Baton의 현재 ERD v2는 **사용자(User)가 재능(Talent)을 등록하고, 구매자가 매칭 제안(MatchProposal)을 생성/수락하며, 이후 크레딧(CreditAccount)과 에스크로(Escrow)로 PURCHASE 거래를 확장하는 구조**다.

현재 코드 기준 확정 테이블은 다음 10개다.

| 구분 | 테이블 | 역할 |
|---|---|---|
| 사용자 | `users` | 회원 정보, 권한, 상태 관리 |
| 재능 | `talent` | 사용자가 등록한 재능 상품 |
| 카테고리 | `category` | 재능 분류 |
| 크레딧 | `credit_account` | 사용 가능 잔액과 에스크로 보류 잔액 관리 |
| 매칭 | `match_proposals` | 구매/교환 제안 상태 관리 |
| 거래 | `trade` | PURCHASE 거래 상태와 거래 당사자 관리 |
| 결과물 | `trade_submission` | 제공자가 제출한 결과물 파일 key와 설명 관리 |
| 에스크로 | `escrow` | 거래 금액 예치, 정산, 환불 상태 관리 |
| 크레딧 이력 | `credit_transaction` | WELCOME, ESCROW_HOLD, ESCROW_RELEASE, REFUND 등 크레딧 원장 |

현재 코드에 없는 테이블은 ERD v2 확정 대상에서 제외한다.

| 제외 대상 | 이유 |
|---|---|
| SWAP 전용 테이블 | MVP 후순위 |

## 2. 핵심 테이블 설명

### 2.1 `users`

사용자 기본 정보를 저장한다.

주요 필드:

| 필드 | 설명 |
|---|---|
| `id` | 사용자 PK |
| `email` | 이메일 |
| `password` | 암호화된 비밀번호 |
| `nickname` | 닉네임 |
| `profileImageUrl` | 프로필 이미지 |
| `introduction` | 자기소개 |
| `trustScore` | 신뢰 점수 |
| `status` | 사용자 상태 |
| `role` | 사용자 권한 |
| `deletedAt` | soft delete 기준 값 |

검수 포인트:

- `email`, `nickname`은 `deletedAt`과 함께 중복 제약을 가진다.
- 기본 상태는 `ACTIVE`, 기본 권한은 `USER`다.
- `deletedAt`은 null이 아니라 과거 고정값으로 초기화된다.

### 2.2 `category`

재능의 분류 정보를 저장한다.

주요 필드:

| 필드 | 설명 |
|---|---|
| `id` | 카테고리 PK |
| `name` | 카테고리명 |
| `sortOrder` | 노출 순서 |
| `active` | 활성 여부 |

검수 포인트:

- 재능 등록 시 카테고리는 필수다.
- 비활성 카테고리에는 재능을 등록할 수 없다.

### 2.3 `talent`

사용자가 등록한 재능 상품을 저장한다.

주요 필드:

| 필드 | 설명 |
|---|---|
| `id` | 재능 PK |
| `authorId` | 작성자 사용자 ID |
| `category` | 카테고리 |
| `title` | 재능 제목 |
| `content` | 재능 설명 |
| `estimatedHours` | 예상 소요 시간 |
| `creditPrice` | 크레딧 가격 |
| `status` | 재능 상태 |
| `viewCount` | 조회 수 |
| `completeCount` | 완료 수 |
| `avgRating` | 평균 평점 |
| `deletedAt` | soft delete 시각 |

검수 포인트:

- `category`는 실제 JPA 연관관계다.
- `authorId`는 `users.id`를 의미하지만, 현재 JPA 연관관계는 아니다.
- 삭제는 물리 삭제가 아니라 `deletedAt`을 채우는 soft delete 방식이다.

### 2.4 `credit_account`

사용자별 크레딧 계좌를 저장한다.

주요 필드:

| 필드 | 설명 |
|---|---|
| `id` | 크레딧 계좌 PK |
| `userId` | 사용자 ID |
| `balance` | 사용 가능 크레딧 |
| `escrowBalance` | 에스크로 보류 크레딧 |
| `version` | 낙관적 락 버전 |

검수 포인트:

- `userId`는 unique다. 사용자 1명당 크레딧 계좌 1개 구조다.
- `balance >= 0`, `escrowBalance >= 0` 체크 제약이 있다.
- 구매 흐름에서 에스크로 보류가 발생하면 `balance`는 감소하고 `escrowBalance`는 증가한다.
- 현재 `userId`는 `users.id`를 의미하지만, JPA 연관관계는 아니다.

### 2.5 `match_proposals`

사용자 간 매칭 제안을 저장한다.

주요 필드:

| 필드 | 설명 |
|---|---|
| `id` | 매칭 제안 PK |
| `providerTalentId` | 제공자 재능 ID |
| `requesterTalentId` | 요청자 재능 ID |
| `requesterId` | 요청자 사용자 ID |
| `providerId` | 제공자 사용자 ID |
| `status` | 매칭 제안 상태 |
| `requestMessage` | 요청 메시지 |
| `respondedAt` | 응답 시각 |

검수 포인트:

- PURCHASE 흐름에서는 `requesterTalentId`가 null일 수 있다.
- 현재 구현된 상태 전이는 `REQUESTED -> ACCEPTED`다.
- enum에는 `REQUESTED`, `ACCEPTED`, `REJECTED`, `CANCELLED`가 있다.
- `providerTalentId`, `requesterTalentId`, `requesterId`, `providerId`는 모두 논리 참조다.

### 2.6 `escrow`

거래 금액을 임시 예치하고, 정산/환불 상태를 관리한다.

주요 필드:

| 필드 | 설명 |
|---|---|
| `id` | 에스크로 PK |
| `tradeId` | 거래 ID |
| `payerId` | 구매자 ID |
| `payeeId` | 판매자 ID |
| `amount` | 예치 금액 |
| `fee` | 수수료 |
| `settlementAmount` | 정산 금액 |
| `status` | 에스크로 상태 |
| `rejectReason` | 거절/분쟁 사유 |
| `expiresAt` | 구매 확정 만료 시각 |
| `reviewRequestedAt` | 검토 요청 시각 |
| `settledAt` | 정산 시각 |

검수 포인트:

- 자체 `Escrow` Entity가 존재한다.
- `tradeId`는 unique이며, 현재 `Trade` Entity와 1:1로 연결되는 논리 관계다.
- `payerId`, `payeeId`는 `users.id`를 의미하지만 JPA 연관관계는 아니다.
- 현재 생성 시 `status = HELD`, `fee = 0`, `settlementAmount = amount`로 설정된다.

## 3. 관계 정리

ERD에서 선으로 연결해야 하는 관계는 아래와 같다.

| From | To | 관계 | 구현 상태 | 설명 |
|---|---|---|---|---|
| `talent.category_id` | `category.id` | N:1 | 실제 FK | 하나의 카테고리에 여러 재능이 속함 |
| `talent.user_id` | `users.id` | N:1 | 논리 FK | 한 사용자가 여러 재능을 등록함 |
| `credit_account.user_id` | `users.id` | 1:1 | 논리 FK | 한 사용자는 하나의 크레딧 계좌를 가짐 |
| `match_proposals.requester_id` | `users.id` | N:1 | 논리 FK | 매칭 제안 요청자 |
| `match_proposals.provider_id` | `users.id` | N:1 | 논리 FK | 매칭 제안 제공자 |
| `match_proposals.provider_talent_id` | `talent.id` | N:1 | 논리 FK | 제공자가 제안받은 재능 |
| `match_proposals.requester_talent_id` | `talent.id` | N:1 | 논리 FK | 요청자 재능. PURCHASE에서는 null 가능 |
| `escrow.payer_id` | `users.id` | N:1 | 논리 FK | 구매자 |
| `escrow.payee_id` | `users.id` | N:1 | 논리 FK | 판매자 |
| `escrow.trade_id` | `trade.id` | 1:1 | 논리 FK | 하나의 거래는 하나의 에스크로를 가짐 |

## 4. 실제 FK와 논리 FK 구분

### 실제 FK

코드에서 JPA 연관관계로 연결된 관계다.

현재 실제 FK로 봐도 되는 관계:

```text
talent.category_id -> category.id
```

근거:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "category_id")
private Category category;
```

### 논리 FK

DB나 JPA에서 직접 연관관계로 묶지는 않았지만, 의미상 다른 테이블의 ID를 참조하는 관계다.

예시:

```text
talent.user_id -> users.id
credit_account.user_id -> users.id
match_proposals.provider_id -> users.id
escrow.payer_id -> users.id
```

검수 시에는 ERD 선을 그리되, 실제 FK가 아니라는 표시를 남기는 것이 좋다.

## 5. PURCHASE 흐름에서의 데이터 연결

현재 MVP PURCHASE 흐름은 아래 순서로 이해하면 된다.

| 순서 | 동작 | 주요 테이블 | 현재 상태 |
|---|---|---|---|
| 1 | 사용자 가입 | `users` | 구현 |
| 2 | 크레딧 계좌 생성 | `credit_account` | 회원가입 시 자동 생성 구현/검증 완료 |
| 3 | 제공자 재능 등록 | `talent`, `category` | 구현 |
| 4 | 구매자 매칭 제안 생성 | `match_proposals` | 구현 |
| 5 | 제공자 매칭 제안 수락 | `match_proposals` | 구현 |
| 6 | 거래 생성 | `trade` | 구현 |
| 7 | 구매자 크레딧 에스크로 보류 | `credit_account` | 구현 |
| 8 | 에스크로 생성 | `escrow` | 구현 |
| 9 | 결과물 제출 | `trade_submission` | 구현 |
| 10 | 구매 확정/정산 | `trade`, `escrow`, `credit_account` | 구현 |
| 11 | 크레딧 거래 이력 기록 | `credit_transaction` | 기록 및 `GET /api/v1/credit/transactions` 조회 구현 |

현재 `MatchProposal` 수락 API는 상태 변경뿐 아니라 Trade 생성, 크레딧 에스크로 보류, Escrow 생성을 함께 수행한다. 회원가입 직후 CreditAccount 자동 생성과 WELCOME 이력 생성도 최신 dev 기준으로 연결되어 있다.

## 6. enum 정리

| enum | 값 |
|---|---|
| `UserStatus` | `ACTIVE`, `DORMANT`, `SUSPENDED`, `WITHDRAWN` |
| `UserRole` | `USER`, `ADMIN` |
| `TalentStatus` | `ACTIVE`, `CLOSED` |
| `MatchProposalStatus` | `REQUESTED`, `ACCEPTED`, `REJECTED`, `CANCELLED` |
| `EscrowStatus` | `HELD`, `RELEASED`, `REFUNDED`, `FROZEN` |

## 7. ERD 이미지에 반드시 반영할 것

- `users`, `category`, `talent`, `talent_attachment`, `credit_account`, `credit_transaction`, `match_proposals`, `trade`, `trade_submission`, `escrow` 최신 테이블
- `CreditAccount.balance`
- `CreditAccount.escrowBalance`
- `Escrow` 자체 테이블
- `MatchProposalStatus`: `REQUESTED`, `ACCEPTED`, `REJECTED`, `CANCELLED`
- `Talent -> Category` 실제 FK
- User 관련 관계는 현재 논리 FK임을 표시
- `Trade`, `TradeSubmission`, `CreditTransaction`은 최신 ERD에 포함

## 8. 최종 검수 체크리스트

- [ ] ERD에 최신 구현 테이블을 반영했다.
- [ ] `trade`, `trade_submission`, `credit_transaction`을 확정 테이블로 반영했다.
- [ ] `credit_account`에 `balance`, `escrow_balance`가 모두 있다.
- [ ] `escrow` 테이블이 별도로 있다.
- [ ] `match_proposals.requester_talent_id`는 nullable로 표시했다.
- [ ] 실제 FK와 논리 FK를 구분했다.
- [ ] 회원가입 후 초기 크레딧 자동 지급과 WELCOME 이력 생성 흐름을 반영했다.
- [ ] 구매 확정 후 Trade/Escrow 재조회 상태 불일치 이슈를 별도 리스크로 표시했다.
