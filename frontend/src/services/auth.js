export async function getAuthSession(options = {}) {
  const response = await fetch('/api/v1/auth/session', { credentials: 'same-origin', cache: 'no-store', ...options });
  if (!response.ok) throw new Error('로그인 상태를 확인하지 못했어요.');
  return response.json();
}

export async function authenticatedFetch(url, options = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = new Headers(options.headers);
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    const session = await getAuthSession();
    if (session.csrfHeader && session.csrfToken) headers.set(session.csrfHeader, session.csrfToken);
  }
  return fetch(url, { ...options, headers, credentials: 'same-origin' });
}

async function submitCredentials(path, values) {
  let response;
  try {
    response = await authenticatedFetch(`/api/v1/users/${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(values),
    });
  } catch {
    throw new Error('서버에 연결하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
  if (!response.ok) {
    const messages = {
      400: '입력 내용을 확인해 주세요. 아이디, 이메일과 비밀번호 형식이 올바른지 확인해 주세요.',
      401: path === 'login' ? '아이디 또는 비밀번호가 일치하지 않아요.' : '보안 정보를 확인하지 못했어요. 새로고침 후 다시 시도해 주세요.',
      403: '보안 정보가 만료되었어요. 다시 시도해 주세요.',
      409: '이미 사용 중인 아이디예요. 다른 아이디를 입력해 주세요.',
      429: '요청이 많아요. 잠시 후 다시 시도해 주세요.',
    };
    throw new Error(messages[response.status] || '요청을 완료하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
  return response.json();
}

export function loginWithPassword(values) {
  return submitCredentials('login', values);
}

export function signupWithPassword(values) {
  return submitCredentials('signup', values);
}

export async function logoutSession() {
  const response = await authenticatedFetch('/api/v1/auth/logout', { method: 'POST' });
  if (!response.ok) throw new Error('로그아웃하지 못했어요. 다시 시도해 주세요.');
}

/** 로그인 상태에서 다른 OAuth 제공자를 같은 회원에 연결한다. */
export async function startSocialLink(provider) {
  const response = await authenticatedFetch(`/api/v1/auth/link/${encodeURIComponent(provider)}`, {
    method: 'POST',
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.detail || body.message || '계정 연결을 시작하지 못했어요.');
  }
  return response.json();
}

export async function unlinkSocialProvider(provider) {
  const response = await authenticatedFetch(`/api/v1/auth/link/${encodeURIComponent(provider)}`, {
    method: 'DELETE',
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({}));
    throw new Error(body.detail || body.message || '계정 연결을 해제하지 못했어요.');
  }
}

/** 현재 소셜 회원에 첫 비밀번호를 설정한다. 기존 비밀번호 변경 API가 아니다. */
export async function setupPasswordLogin(password) {
  const response = await authenticatedFetch('/api/v1/auth/me/password', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ password }),
  });
  if (!response.ok) {
    const messages = {
      400: '비밀번호 형식과 소셜 계정 연결 상태를 확인해 주세요.',
      401: '로그인 후 다시 시도해 주세요.',
      403: '인증 정보가 만료되었어요. 새로고침 후 다시 시도해 주세요.',
      409: '이미 비밀번호 로그인이 설정되어 있어요. 새로고침해 주세요.',
    };
    throw new Error(messages[response.status] || '비밀번호 설정을 완료하지 못했어요. 다시 시도해 주세요.');
  }
  return response.json();
}
