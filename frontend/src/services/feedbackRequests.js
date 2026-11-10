import { authenticatedFetch } from './auth.js';

async function request(path, options = {}) {
  const response = await authenticatedFetch(`/api/v1${path}`, { credentials: 'same-origin', ...options });
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

const json = (method, body) => ({ method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

// 피드백 게시판 목록 (WBS 4.1 전체, 4.3 category=FRONTEND|BACKEND|FULLSTACK, 4.4 mine=true)
export const getFeedbackRequests = ({ category, mine } = {}, signal) => {
  const params = new URLSearchParams();
  if (category) params.set('category', category);
  if (mine) params.set('mine', 'true');
  const query = params.toString();
  return request(`/feedback-requests${query ? `?${query}` : ''}`, { signal });
};

// 피드백 게시글 상세
export const getFeedbackRequest = (id, signal) => request(`/feedback-requests/${id}`, { signal });

// 피드백 댓글 목록 (WBS 4.6)
export const getFeedbackComments = (id, signal) => request(`/feedback-requests/${id}/comments`, { signal });

// 피드백 댓글 작성 (WBS 4.7). section: 요청한 이력서 항목. null이면 이력서 전체
export const createFeedbackComment = (id, body) => request(`/feedback-requests/${id}/comments`, json('POST', body));

export const deleteFeedbackComment = (commentId) => request(`/feedbacks/${commentId}`, { method: 'DELETE' });

// 피드백 채택 (WBS 4.10). 요청한 본인만, 요청 하나에 하나만 채택할 수 있다
export const adoptFeedback = (commentId) => request(`/feedbacks/${commentId}/adopt`, { method: 'POST' });

// 피드백 요청 (WBS 3.2 = 4.2). commonResumeId와 targetResumeId 중 하나만 보낸다.
// sections: BASIC, SKILLS, PROJECTS, AWARDS, INTRODUCTION, ANSWERS
export const createFeedbackRequest = (body) => request('/feedback-requests', json('POST', body));

// 피드백 요청 취소 (WBS 3.3)
export const cancelFeedbackRequest = (id) => request(`/feedback-requests/${id}`, { method: 'DELETE' });
