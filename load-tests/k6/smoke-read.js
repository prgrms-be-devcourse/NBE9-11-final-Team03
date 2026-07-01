import { sleep } from 'k6';
import { api, assertApiSuccess, createTalent, login, signupUser, uniqueSuffix } from './lib/baton-client.js';

// 최초 실행용 안전 점검으로, 소수의 조회 API만 가볍게 호출합니다.
// 필요하면 SMOKE_VUS와 SMOKE_DURATION 환경변수로 부하를 조정합니다.
export const options = {
  vus: Number(__ENV.SMOKE_VUS || 1),
  duration: __ENV.SMOKE_DURATION || '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

// setup은 VU 실행 전에 한 번만 동작하며 반복 사용할 테스트 데이터를 준비합니다.
export function setup() {
  const buyer = signupUser('smokeb');
  const seller = signupUser('smokes');
  const buyerToken = login(buyer.email, buyer.password);
  const sellerToken = login(seller.email, seller.password);
  const talentId = createTalent(sellerToken, uniqueSuffix(), 3000);

  return { buyerToken, talentId };
}

// 각 VU는 설정된 시간 동안 이 조회 흐름을 반복합니다.
export default function (data) {
  assertApiSuccess(api('GET', '/api/v1/talents?size=10', null, data.buyerToken), 'talent list');
  assertApiSuccess(api('GET', `/api/v1/talents/${data.talentId}`, null, data.buyerToken), 'talent detail');
  assertApiSuccess(api('GET', '/api/v1/credit/balance', null, data.buyerToken), 'credit balance');
  sleep(1);
}
