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
    headers.set(session.csrfHeader, session.csrfToken);
  }
  return fetch(url, { ...options, headers, credentials: 'same-origin' });
}

export async function logoutSession() {
  const response = await authenticatedFetch('/api/v1/auth/logout', { method: 'POST' });
  if (!response.ok) throw new Error('로그아웃하지 못했어요. 다시 시도해 주세요.');
}
