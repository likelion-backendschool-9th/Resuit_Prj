import { authenticatedFetch } from './auth.js';

async function request(path, options = {}) {
  const response = await authenticatedFetch(`/api/v1/feedback-requests${path}`, { credentials: 'same-origin', ...options });
  if (!response.ok) {
    const error = await response.json().catch(() => null);
    const failure = new Error(response.status === 401 ? '로그인 후 이용할 수 있어요.'
      : error?.message || '피드백 요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.');
    failure.status = response.status;
    failure.code = error?.code;
    throw failure;
  }
  return response.status === 204 ? null : response.json();
}

// 피드백 요청 (WBS 3.2 = 4.2). commonResumeId와 targetResumeId 중 하나만 보낸다.
// sections: BASIC, SKILLS, PROJECTS, AWARDS, INTRODUCTION, ANSWERS
export const createFeedbackRequest = (body) => request('', {
  method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
});

// 피드백 요청 취소 (WBS 3.3)
export const cancelFeedbackRequest = (id) => request(`/${id}`, { method: 'DELETE' });
