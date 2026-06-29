import { sleep } from 'k6';
import {
  api,
  assertApiSuccess,
  createTalent,
  login,
  parseData,
  signupUser,
  uniqueSuffix,
} from './lib/baton-client.js';

// 쓰기 작업이 많은 PURCHASE 전체 흐름 smoke 테스트입니다.
// 매 반복마다 사용자, 재능, 제안, 거래, 크레딧 데이터가 저장되므로 VU와 반복 횟수를 낮게 유지합니다.
export const options = {
  vus: Number(__ENV.PURCHASE_VUS || 1),
  iterations: Number(__ENV.PURCHASE_ITERATIONS || 1),
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
  },
};

export default function () {
  const suffix = uniqueSuffix();

  // 1. 독립적인 구매자/판매자 계정과 판매자의 재능을 준비합니다.
  const buyer = signupUser('buyload');
  const seller = signupUser('selload');
  const buyerToken = login(buyer.email, buyer.password);
  const sellerToken = login(seller.email, seller.password);
  const talentId = createTalent(sellerToken, suffix, 5000);

  // 2. 구매자가 PURCHASE 제안을 생성하고 판매자가 수락합니다.
  const proposalRes = api('POST', '/api/v1/match-proposals', {
    requesterTalentId: null,
    providerId: seller.userId,
    providerTalentId: talentId,
    requestMessage: 'k6 purchase flow smoke request',
  }, buyerToken);
  assertApiSuccess(proposalRes, 'create proposal');
  const proposalId = parseData(proposalRes).id;

  const acceptRes = api(
    'PATCH',
    `/api/v1/match-proposals/${proposalId}/accept`,
    null,
    sellerToken,
    { 'Idempotency-Key': `k6-accept-${suffix}` },
  );
  assertApiSuccess(acceptRes, 'accept proposal');

  // 3. 제안 수락으로 생성된 Trade를 거래 목록에서 찾습니다.
  const tradesRes = api('GET', '/api/v1/trade?size=20', null, buyerToken);
  assertApiSuccess(tradesRes, 'trade list');
  const trades = parseData(tradesRes).content || [];
  const trade = trades.find((row) => row.sellerId === seller.userId && row.talentId === talentId);

  if (!trade) {
    throw new Error(`Trade not found for talent ${talentId}`);
  }

  // 4. 판매자가 결과물을 제출하고 구매자가 구매를 확정합니다.
  const submitRes = api('POST', `/api/v1/trade/${trade.tradeId}/submission`, {
    fileKey: `trades/${trade.tradeId}/k6-result-${suffix}.txt`,
    description: 'k6 purchase smoke result',
  }, sellerToken);
  assertApiSuccess(submitRes, 'submit result');

  const confirmRes = api('PATCH', `/api/v1/trade/${trade.tradeId}/confirm`, null, buyerToken);
  assertApiSuccess(confirmRes, 'confirm purchase');

  // 5. 최종 잔액 조회를 통해 정산 이후에도 관련 API가 정상 응답하는지 확인합니다.
  assertApiSuccess(api('GET', '/api/v1/credit/balance', null, buyerToken), 'buyer balance');
  assertApiSuccess(api('GET', '/api/v1/credit/balance', null, sellerToken), 'seller balance');
  sleep(1);
}
