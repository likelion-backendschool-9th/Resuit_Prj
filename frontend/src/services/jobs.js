import { authenticatedFetch } from './auth.js';

async function request(path, { baseUrl = '', ...options } = {}) {
  const response = await authenticatedFetch(`${baseUrl}/api/v1/jobs${path}`, {
    cache: 'no-store', signal: AbortSignal.timeout(10000), ...options,
  });
  if (!response.ok) {
    const messages = { 400: '입력 내용을 확인해 주세요. 특화 공고에는 요구 문항이 필요하며 게시할 마감일은 오늘 이후여야 해요.', 401: '로그인 후 이용해 주세요.', 403: '기업 계정의 본인 공고만 관리할 수 있어요. 기업 회원으로 가입한 계정으로 로그인했는지 확인해 주세요.', 404: '공고나 지원 내역을 찾을 수 없어요. 삭제되거나 지원이 취소되었을 수 있습니다.', 410: '제출 이력서의 3개월 보존 기간이 만료되어 더 이상 열람할 수 없어요.' };
    const error = new Error(messages[response.status] || '공고 요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
    error.status = response.status;
    throw error;
  }
  return response.status === 204 ? null : response.json();
}
const json = (method, body) => ({ method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
export const getJobs = ({ type = 'all', page = 0, ...options } = {}) => request(`?page=${page}&size=20${type !== 'all' ? `&type=${encodeURIComponent(type.toUpperCase())}` : ''}`, options);
export const getJob = (id, options) => request(`/${encodeURIComponent(id)}`, options);
export const getMyJobs = (page = 0, options) => request(`/me?page=${page}&size=20`, options);
// Company-only application status: each response includes the context required by its screen.
export const getPostedJobs = (page = 0, options) => request(`/me/posted?page=${page}&size=20`, options);
export const getCompanyApplicants = (jobId, page = 0, options) => request(`/me/posted/${encodeURIComponent(jobId)}/applications?page=${page}&size=20`, options);
export const getCompanyApplication = (jobId, applicationId, options) => request(`/me/posted/${encodeURIComponent(jobId)}/applications/${encodeURIComponent(applicationId)}`, options);
export const getJobDrafts = (page = 0, options) => request(`/me/drafts?page=${page}&size=20`, options);
export const getJobDraft = (id, options) => request(`/me/drafts/${encodeURIComponent(id)}`, options);
export const createJob = (body) => request('', json('POST', body));
export const updateJob = (id, body) => request(`/${encodeURIComponent(id)}`, json('PATCH', body));
export const createJobDraft = (body) => request('/me/drafts', json('POST', body));
export const updateJobDraft = (id, body) => request(`/me/drafts/${encodeURIComponent(id)}`, json('PUT', body));
export const deleteJob = (id) => request(`/${encodeURIComponent(id)}`, { method: 'DELETE' });

// Public DTO → existing card model. Dates are shown literally to keep SSR and hydration identical.
export function toJobCard(job) {
  const company = job.company?.nickname || job.company?.username || '기업';
  return { ...job, type: job.type?.toLowerCase(), company,
    initial: company.slice(0, 1), av: 'co2',
    dday: job.status === 'CLOSED' ? '마감' : job.deadline ? `${job.deadline} 마감` : '마감일 미정',
    meta: [job.location, job.employmentType, job.career].filter(Boolean).join(' · ') };
}
