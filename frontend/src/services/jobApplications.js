import { authenticatedFetch } from './auth.js';

// 공고 지원 API (backend/resuit/JOB_APPLICATION_API.md). 오류는 서버가 보낸 안내 문구를 그대로 보여 준다.
async function request(jobId, path, options = {}) {
  const response = await authenticatedFetch(`/api/v1/jobs/${encodeURIComponent(jobId)}${path}`, { cache: 'no-store', ...options });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    const error = new Error(response.status === 401 ? '로그인 후 지원할 수 있어요.'
      : body?.message || '지원 요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? null : response.json();
}

const json = (body) => ({ method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

// 내 현재 지원: { applied, jobId, id, resumeType: COMMON|TARGET, resumeId, status, appliedAt }
export const getMyApplication = (jobId, signal) => request(jobId, '/applications/me', { signal });

// 일반 공고는 공통 이력서로 지원한다
export const applyWithCommonResume = (jobId, commonResumeId) => request(jobId, '/applications', json({ commonResumeId }));

// 특화 공고는 그 공고용 특화 이력서로 지원한다
export const applyWithTargetResume = (jobId, targetResumeId) => request(jobId, '/special-applications', json({ targetResumeId }));

// 지원 취소. 공고가 아직 모집 중이면 다시 지원할 수 있다
export const cancelApplication = (jobId) => request(jobId, '/applications', { method: 'DELETE' });
