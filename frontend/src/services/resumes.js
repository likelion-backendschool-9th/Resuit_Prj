import { authenticatedFetch } from './auth.js';

async function request(path, options = {}) {
  const response = await authenticatedFetch(`/api/v1${path}`, { credentials: 'same-origin', ...options });
  if (!response.ok) {
    if (response.status === 401) throw new Error('로그인 후 이용할 수 있어요.');
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || '이력서를 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
  return response.status === 204 ? null : response.json();
}

const json = (method, body) => ({ method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

// 공통 이력서 (WBS 3.4 ~ 3.8)
export const getCommonResumes = (signal) => request('/common-resumes', { signal });
export const getCommonResume = (id, signal) => request(`/common-resumes/${id}`, { signal });
export const createCommonResume = (resume) => request('/common-resumes', json('POST', resume));
export const updateCommonResume = (id, changes) => request(`/common-resumes/${id}`, json('PATCH', changes));
export const deleteCommonResume = (id) => request(`/common-resumes/${id}`, { method: 'DELETE' });
