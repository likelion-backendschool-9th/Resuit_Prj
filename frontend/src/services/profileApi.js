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
    throw new Error(`프로필 요청 실패 (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

export const profileApi = {
  getPublic: (username, viewer) => request(`/users/${encodeURIComponent(username)}`, { username: viewer }),
  getMine: (username) => request('/users/me', { username }),
  updateMine: (username, body) => request('/users/me', { username, method: 'PATCH', body: JSON.stringify(body) }),
  pinResume: (username, cResumeId) => request('/users/me/pinned-resume', {
    username, method: 'PUT', body: JSON.stringify({ cResumeId }),
  }),
  setVisibility: (username, resumeId, isPublic) => request(`/users/me/resumes/${resumeId}/visibility`, {
    username, method: 'PUT', body: JSON.stringify({ isPublic }),
  }),
  displayBadge: (username, badgeId) => request('/users/me/displayed-badge', {
    username, method: 'PUT', body: JSON.stringify({ badgeId }),
  }),
  getPosts: (username, type, page = 0) => request(
    `/users/${encodeURIComponent(username)}/tweets?page=${page}&size=20${type ? `&type=${type}` : ''}`),
  getFeedbacks: (username, page = 0) => request(
    `/users/${encodeURIComponent(username)}/feedbacks?page=${page}&size=20`),
  getReviewerProgress: (username) => request('/badges/me/reviewer-progress', { username }),
  getIntegrations: (username) => request('/users/me/integrations', { username }),
  connectBlog: (username, platform, blogId) => request('/users/me/integrations/blog', {
    username, method: 'PUT', body: JSON.stringify({ platform, blogId }),
  }),
  disconnectIntegration: (username, provider) => request(`/users/me/integrations/${provider}`, {
    username, method: 'DELETE',
  }),
  getRepos: (username, pinned) => {
    const query = pinned === true ? '?pinned=true' : '';
    return request(`/users/${encodeURIComponent(username)}/github/repos${query}`);
  },
  getContributions: (username) => request(`/users/${encodeURIComponent(username)}/github/contributions`),
  pinRepos: (username, repoFullNames) => request('/users/me/github/pinned-repos', {
    username, method: 'PUT', body: JSON.stringify({ repoFullNames }),
  }),
  getBlogPosts: (username) => request(`/users/${encodeURIComponent(username)}/blog-posts?size=5`),
  follow: (username, viewer) => request(`/users/${encodeURIComponent(username)}/follow`, {
    username: viewer, method: 'POST',
  }),
  unfollow: (username, viewer) => request(`/users/${encodeURIComponent(username)}/follow`, {
    username: viewer, method: 'DELETE',
  }),
};
