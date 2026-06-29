import { sleep } from 'k6';
import {
  api,
  assertApiSuccess,
  createTalent,
  login,
  signupUser,
  uniqueSuffix,
} from '../lib/baton-client.js';

/*
 * 이 파일은 k6 문법과 Baton 부하 테스트 구조를 설명하기 위한 대표 예제입니다.
 * 실제 조회 부하 테스트는 ../read-apis.js를 사용하고, 설정을 이해하거나 새로운
 * 시나리오를 작성할 때 이 파일을 참고합니다.
 *
 * k6 실행 순서
 * 1. 파일을 읽고 options 설정을 해석합니다.
 * 2. setup()을 전체 테스트에서 한 번 실행합니다.
 * 3. setup() 반환값을 모든 VU에 복사해 전달합니다.
 * 4. 각 VU가 default 함수를 독립적으로 반복 실행합니다.
 * 5. 테스트 종료 후 threshold 충족 여부에 따라 프로세스 종료 코드를 결정합니다.
 */

// __ENV는 PowerShell 또는 Docker Compose에서 전달한 환경변수를 읽는 k6 전역 객체입니다.
// 값이 없으면 오른쪽 기본값을 사용합니다. __ENV 값은 문자열이므로 숫자는 Number로 변환합니다.
const TARGET_VUS = Number(__ENV.EXAMPLE_VUS || 3);
const HOLD_DURATION = __ENV.EXAMPLE_HOLD || '30s';

export const options = {
  /*
   * stages는 테스트 시간에 따라 가상 사용자(VU) 수를 변경합니다.
   *
   * 아래 설정의 의미:
   * - 10초 동안 VU를 0명에서 TARGET_VUS까지 점진적으로 증가
   * - HOLD_DURATION 동안 TARGET_VUS를 유지
   * - 10초 동안 VU를 다시 0명으로 감소
   *
   * 갑자기 많은 요청을 보내지 않고 어느 구간부터 응답이 느려지는지 확인하기 위해
   * ramp-up과 ramp-down 단계를 둡니다.
   */
  stages: [
    { duration: '10s', target: TARGET_VUS },
    { duration: HOLD_DURATION, target: TARGET_VUS },
    { duration: '10s', target: 0 },
  ],

  /*
   * thresholds는 단순 측정값이 아니라 테스트의 합격/실패 기준입니다.
   * 기준을 위반하면 k6 프로세스가 실패 종료되므로 CI에서도 품질 게이트로 사용할 수 있습니다.
   */
  thresholds: {
    // 네트워크 오류와 예상하지 않은 HTTP 상태 응답 비율이 1% 미만이어야 합니다.
    http_req_failed: ['rate<0.01'],

    // 전체 HTTP 요청 중 95%가 1초 안에 완료되어야 합니다.
    // 평균값만 보면 일부 매우 느린 요청이 가려질 수 있어 p95를 함께 봅니다.
    http_req_duration: ['p(95)<1000'],
  },
};

/*
 * setup()은 VU 수와 관계없이 테스트 시작 전에 한 번만 실행됩니다.
 *
 * 회원가입이나 테스트 데이터 생성처럼 반복할 필요가 없는 준비 작업을 여기에 둡니다.
 * default 함수 안에서 회원가입을 하면 VU와 반복 횟수만큼 DB 데이터가 계속 생성되므로
 * 조회 부하 테스트에서는 setup에서 계정과 재능을 한 번만 준비합니다.
 */
export function setup() {
  const buyer = signupUser('exampleb');
  const seller = signupUser('examples');

  const buyerToken = login(buyer.email, buyer.password);
  const sellerToken = login(seller.email, seller.password);
  const talentId = createTalent(sellerToken, uniqueSuffix(), 3000);

  // 반환값은 JSON으로 직렬화된 뒤 모든 VU의 default 함수 인자로 전달됩니다.
  // 비밀번호처럼 반복 호출에 필요하지 않은 민감 정보는 반환하지 않습니다.
  return {
    buyerToken,
    talentId,
  };
}

/*
 * default 함수는 가상 사용자 한 명의 행동을 나타냅니다.
 * k6는 stage가 유지되는 동안 각 VU에서 이 함수를 계속 반복합니다.
 * VU끼리는 JavaScript 실행 컨텍스트를 공유하지 않으므로 전역 가변 상태에 의존하지 않습니다.
 */
export default function (data) {
  // api()는 공통 클라이언트 함수이며 BASE_URL과 Authorization 헤더 처리를 담당합니다.
  const listRes = api('GET', '/api/v1/talents?size=20', null, data.buyerToken);

  // check는 요청을 즉시 중단시키는 assertion이 아니라 성공/실패 통계를 기록합니다.
  // 공통 함수는 HTTP 2xx와 ApiResponse.success를 각각 검사합니다.
  assertApiSuccess(listRes, '재능 목록 조회');

  const detailRes = api(
    'GET',
    `/api/v1/talents/${data.talentId}`,
    null,
    data.buyerToken,
  );
  assertApiSuccess(detailRes, '재능 상세 조회');

  const balanceRes = api('GET', '/api/v1/credit/balance', null, data.buyerToken);
  assertApiSuccess(balanceRes, '크레딧 잔액 조회');

  /*
   * sleep은 실제 사용자가 화면을 읽거나 다음 동작을 선택하는 대기 시간을 모사합니다.
   * sleep이 없으면 각 VU가 가능한 최대 속도로 요청해 현실적인 사용자 부하가 아니라
   * 서버 한계까지 밀어붙이는 스트레스 테스트에 가까워집니다.
   */
  sleep(1);
}
