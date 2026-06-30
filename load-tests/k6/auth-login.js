import { sleep } from 'k6';
import { api, assertApiSuccess, signupUser } from './lib/baton-client.js';

// 사용자 조회, BCrypt 검증, JWT 생성, RefreshToken 저장을 포함한 로그인 비용을 측정합니다.
export const options = {
  stages: [
    { duration: '30s', target: Number(__ENV.AUTH_VUS || 3) },
    { duration: __ENV.AUTH_HOLD || '1m', target: Number(__ENV.AUTH_VUS || 3) },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
  },
};

// 반복 로그인 요청을 시작하기 전에 유효한 계정 하나를 생성합니다.
export function setup() {
  return signupUser('authload');
}

// 각 VU는 동일한 유효 계정으로 로그인을 반복합니다.
export default function (user) {
  const res = api('POST', '/api/v1/auth/login', {
    email: user.email,
    password: user.password,
  });

  assertApiSuccess(res, 'login');
  sleep(1);
}
