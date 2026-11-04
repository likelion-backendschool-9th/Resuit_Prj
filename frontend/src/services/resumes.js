import { authenticatedFetch } from './auth.js';

async function request(path, options = {}) {
  const response = await authenticatedFetch(`/api/v1${path}`, { credentials: 'same-origin', ...options });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    const failure = new Error(response.status === 401 ? '로그인 후 이용할 수 있어요.'
      : error?.message || '이력서를 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
    failure.status = response.status;
    failure.code = error?.code;
    throw failure;
  }
  return response.status === 204 ? null : response.json();
}

const json = (method, body) => ({ method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

// 이력서 탭 전체 목록 (WBS 3.1): { commonResumes, targetResumes }
export const getAllResumes = (signal) => request('/resumes', { signal });

// 공통 이력서 (WBS 3.4 ~ 3.8)
export const getCommonResumes = (signal) => request('/common-resumes', { signal });
export const getCommonResume = (id, signal) => request(`/common-resumes/${id}`, { signal });
export const createCommonResume = (resume) => request('/common-resumes', json('POST', resume));
export const updateCommonResume = (id, changes) => request(`/common-resumes/${id}`, json('PATCH', changes));
export const deleteCommonResume = (id) => request(`/common-resumes/${id}`, { method: 'DELETE' });

// 특화 이력서 (WBS 3.9 ~ 3.13). mode: BASE(공통 이력서 복사) | BLANK(빈 이력서)
export const getTargetResumes = (signal) => request('/target-resumes', { signal });
export const getTargetResume = (id, signal) => request(`/target-resumes/${id}`, { signal });
export const createTargetResume = (resume) => request('/target-resumes', json('POST', resume));
export const updateTargetResume = (id, changes) => request(`/target-resumes/${id}`, json('PATCH', changes));
export const deleteTargetResume = (id) => request(`/target-resumes/${id}`, { method: 'DELETE' });
