import { getAuthSession } from './auth.js';

const ROOT = '/api/v1';

async function request(path, { username, ...options } = {}) {
  const method = (options.method || 'GET').toUpperCase();
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(username ? { 'X-Username': username } : {}),
    ...options.headers,
  };
  if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
    try {
      const session = await getAuthSession();
      if (session?.csrfHeader && session?.csrfToken) {
        headers[session.csrfHeader] = session.csrfToken;
      }
    } catch {
      /* local X-Username 모드는 CSRF 없이 동작할 수 있다 */
    }
  }
  const response = await fetch(`${ROOT}${path}`, { ...options, method, headers, credentials: 'same-origin' });
  if (!response.ok) {
    throw new Error(`이력서 요청 실패 (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

export const resumeApi = {
  listCommon: (username) => request('/common-resumes', { username }),
  createCommon: (username, body) => request('/common-resumes', {
    username, method: 'POST', body: JSON.stringify(body),
  }),
  updateCommon: (username, id, body) => request(`/common-resumes/${id}`, {
    username, method: 'PATCH', body: JSON.stringify(body),
  }),
  deleteCommon: (username, id) => request(`/common-resumes/${id}`, {
    username, method: 'DELETE',
  }),
};
