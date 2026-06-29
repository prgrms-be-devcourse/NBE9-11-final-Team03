import http from 'k6/http';
import { check } from 'k6';

// 모든 Baton k6 시나리오에서 공통으로 사용하는 API 클라이언트입니다.
export const BASE_URL = __ENV.BASE_URL || 'http://54.116.23.255';

// 반복 실행 시 dev DB에서 데이터가 충돌하지 않도록 고유한 접미사를 생성합니다.
export function uniqueSuffix() {
  return `${Date.now()}-${Math.floor(Math.random() * 1000000)}`;
}

export function jsonHeaders(token = null, extra = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...extra,
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return { headers };
}

// 시나리오 파일은 사용자 행동에만 집중할 수 있도록 HTTP 호출을 한곳에서 처리합니다.
export function api(method, path, body = null, token = null, extraHeaders = {}) {
  const url = `${BASE_URL}${path}`;
  const params = jsonHeaders(token, extraHeaders);
  const payload = body === null ? null : JSON.stringify(body);

  switch (method) {
    case 'GET':
      return http.get(url, params);
    case 'POST':
      return http.post(url, payload, params);
    case 'PATCH':
      return http.patch(url, payload, params);
    case 'PUT':
      return http.put(url, payload, params);
    case 'DELETE':
      return http.del(url, payload, params);
    default:
      throw new Error(`Unsupported method: ${method}`);
  }
}

// Baton 공통 ApiResponse 응답에서 data 필드만 추출합니다.
export function parseData(res) {
  const body = safeJson(res);
  return body && body.data;
}

// 네트워크 실패나 JSON이 아닌 응답 때문에 검사 로직 자체가 중단되지 않도록 처리합니다.
function safeJson(res) {
  if (!res || !res.body) {
    return null;
  }

  try {
    return res.json();
  } catch (_) {
    return null;
  }
}

// HTTP 상태와 ApiResponse 성공 여부를 각각 k6 검사 결과로 기록합니다.
export function assertApiSuccess(res, name) {
  check(res, {
    [`${name}: HTTP 2xx`]: (r) => r.status >= 200 && r.status < 300,
    [`${name}: ApiResponse success`]: (r) => {
      const body = safeJson(r);
      return body && body.success === true;
    },
  });
}

// setup 단계 또는 PURCHASE 반복 실행마다 독립적인 테스트 사용자를 생성합니다.
export function signupUser(prefix) {
  const suffix = uniqueSuffix();
  // slice(0, 10)이 타임스탬프 앞자리만 남겨 같은 prefix 닉네임이 'buyload178'처럼
  // 수 시간 동안 고정·중복되던 문제를 막기 위해, prefix 앞 3자 + base36 랜덤 7자
  // (약 780억 조합)로 3~10자 제약을 만족하는 고유 닉네임을 생성합니다.
  const rand = Math.random().toString(36).slice(2).padEnd(7, '0').slice(0, 7);
  const nickname = `${prefix.slice(0, 3)}${rand}`.replace(/[^a-zA-Z0-9]/g, '').slice(0, 10);
  const email = `${prefix}-${suffix}@example.com`;
  const password = 'Qa!2026xy';

  const res = api('POST', '/api/v1/auth/signup', {
    email,
    password,
    nickname,
    profileImageUrl: 'https://example.com/profile.png',
    introduction: `${prefix} load test account`,
  });

  assertApiSuccess(res, `${prefix} signup`);

  const data = parseData(res);
  if (!data) {
    throw new Error(`${prefix} signup failed: status=${res.status}, error=${res.error || 'unknown'}`);
  }

  return {
    email,
    password,
    userId: data.id,
  };
}

// 로그인 후 인증 API 호출에 사용할 accessToken을 반환합니다.
export function login(email, password) {
  const res = api('POST', '/api/v1/auth/login', { email, password });
  assertApiSuccess(res, 'login');
  const data = parseData(res);
  if (!data) {
    throw new Error(`login failed: status=${res.status}, error=${res.error || 'unknown'}`);
  }
  return data.accessToken;
}

// 인증된 판매자 계정이 소유하는 테스트 재능을 생성합니다.
export function createTalent(token, titleSuffix, creditPrice = 5000) {
  const res = api('POST', '/api/v1/talents', {
    categoryId: 1,
    title: `QA load talent ${titleSuffix}`.slice(0, 100),
    content: 'QA load test talent content',
    estimatedHours: 2,
    creditPrice,
  }, token);

  assertApiSuccess(res, 'create talent');
  const data = parseData(res);
  if (!data) {
    throw new Error(`create talent failed: status=${res.status}, error=${res.error || 'unknown'}`);
  }
  return data.talentId;
}
