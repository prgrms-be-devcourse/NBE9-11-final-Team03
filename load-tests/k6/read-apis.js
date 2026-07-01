import { sleep } from 'k6';
import { api, assertApiSuccess, createTalent, login, signupUser, uniqueSuffix } from './lib/baton-client.js';

// QueryDSL 목록/검색과 잔액/내역 조회를 대상으로 하는 읽기 중심 부하 테스트입니다.
// stages는 VU를 점진적으로 늘리고 유지한 뒤 안전하게 감소시킵니다.
export const options = {
  stages: [
    { duration: '30s', target: Number(__ENV.READ_VUS || 5) },
    { duration: __ENV.READ_HOLD || '2m', target: Number(__ENV.READ_VUS || 5) },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

// 모든 조회 VU가 함께 사용할 구매자, 판매자, 재능 데이터를 한 번만 생성합니다.
export function setup() {
  const buyer = signupUser('readb');
  const seller = signupUser('reads');
  const buyerToken = login(buyer.email, buyer.password);
  const sellerToken = login(seller.email, seller.password);
  const talentId = createTalent(sellerToken, uniqueSuffix(), 5000);

  return { buyerToken, talentId };
}

// 로그인한 사용자가 Baton의 주요 조회 화면을 탐색하는 동작을 재현합니다.
export default function (data) {
  assertApiSuccess(api('GET', '/api/v1/talents?size=20', null, data.buyerToken), 'talent list');
  assertApiSuccess(api('GET', '/api/v1/talents/search?size=20&sort=LATEST', null, data.buyerToken), 'talent search');
  assertApiSuccess(api('GET', `/api/v1/talents/${data.talentId}`, null, data.buyerToken), 'talent detail');
  assertApiSuccess(api('GET', '/api/v1/credit/balance', null, data.buyerToken), 'credit balance');
  assertApiSuccess(api('GET', '/api/v1/credit/transactions?size=20', null, data.buyerToken), 'credit transactions');
  assertApiSuccess(api('GET', '/api/v1/trade?size=20', null, data.buyerToken), 'trade list');
  sleep(1);
}
