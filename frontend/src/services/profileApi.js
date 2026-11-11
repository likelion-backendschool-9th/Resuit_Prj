import { authenticatedFetch, getAuthSession } from './auth.js';

const ROOT = '/api/v1';

const IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/gif', 'image/webp']);
const MAX_PROFILE_IMAGE_BYTES = 5 * 1024 * 1024;

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
    let detail = '';
    try {
      const body = await response.json();
      detail = body.detail || body.message || '';
    } catch {
      /* ignore */
    }
    throw new Error(detail || `프로필 요청 실패 (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

export const profileApi = {
  getPublic: (username, viewer) => request(`/users/${encodeURIComponent(username)}`, { username: viewer }),
  getMine: (username) => request('/users/me', { username }),
  updateMine: (username, body) => request('/users/me', { username, method: 'PATCH', body: JSON.stringify(body) }),
  /** 게시글과 같은 임시 업로드 API. 응답 path를 PATCH profileImageUrl에 넣는다. */
  uploadProfileImage: async (file) => {
    if (!file) throw new Error('업로드할 파일이 없습니다.');
    if (!IMAGE_TYPES.has(file.type)) {
      throw new Error('jpg, png, gif, webp 이미지만 올릴 수 있습니다.');
    }
    if (file.size > MAX_PROFILE_IMAGE_BYTES) {
      throw new Error('프로필 사진은 5MB 이하여야 합니다.');
    }
    const body = new FormData();
    body.append('file', file);
    const response = await authenticatedFetch(`${ROOT}/posts/uploads`, { method: 'POST', body });
    if (!response.ok) {
      let detail = '';
      try {
        const payload = await response.json();
        detail = payload.detail || payload.message || '';
      } catch {
        /* ignore */
      }
      throw new Error(detail || `이미지 업로드 실패 (${response.status})`);
    }
    const payload = await response.json();
    if (payload.mediaType !== 'IMAGE' || !payload.path) {
      throw new Error('이미지 업로드 응답이 올바르지 않습니다.');
    }
    return payload.path;
  },
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
  syncGithub: (username) => request('/users/me/integrations/github/sync', {
    username, method: 'POST',
  }),
  syncBlog: (username) => request('/users/me/integrations/blog/sync', {
    username, method: 'POST',
  }),
  connectBlog: (username, platform, blogId, rssUrl) => request('/users/me/integrations/blog', {
    username,
    method: 'PUT',
    body: JSON.stringify({
      platform,
      blogId: blogId || null,
      rssUrl: rssUrl || null,
    }),
  }),
  startBlogVerify: (username, platform, blogId, rssUrl) => request('/users/me/integrations/blog/verify', {
    username,
    method: 'POST',
    body: JSON.stringify({
      platform,
      blogId: blogId || null,
      rssUrl: rssUrl || null,
    }),
  }),
  confirmBlogVerify: (username) => request('/users/me/integrations/blog/verify/confirm', {
    username,
    method: 'POST',
  }),
  pendingTistoryBlogs: (username) => request('/users/me/integrations/tistory/pending', { username }),
  connectTistoryBlog: (username, name) => request('/users/me/integrations/tistory', {
    username,
    method: 'POST',
    body: JSON.stringify({ name }),
  }),
  disconnectIntegration: (username, provider) => request(`/users/me/integrations/${provider}`, {
    username, method: 'DELETE',
  }),
  getRepos: (username, pinned) => {
    const query = pinned === true ? '?pinned=true' : '';
    return request(`/users/${encodeURIComponent(username)}/github/repos${query}`);
  },
  getContributions: (username, { from, to } = {}) => {
    const query = new URLSearchParams();
    if (from) query.set('from', from);
    if (to) query.set('to', to);
    const suffix = query.toString() ? `?${query}` : '';
    return request(`/users/${encodeURIComponent(username)}/github/contributions${suffix}`);
  },
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
