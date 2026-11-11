import { authenticatedFetch } from './auth.js';

// 유료 피드백(8.1~8.6) API. 화면은 이 함수들만 부르고 주소나 응답 처리 방식은 모른다.
const ROOT = '/api/v1';

// 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 기본 문구를 따로 둔다.
async function read(response, fallback) {
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.message || fallback[response.status] || fallback.default);
    error.status = response.status;
    throw error;
  }
  return data;
}

const COMMON = { 401: '로그인이 필요해요.', 403: '이 요청을 볼 수 있는 사람이 아니에요.', 404: '찾을 수 없어요. 현직자 인증이 취소됐을 수 있어요.' };
const json = (method, body) => ({ method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
const query = (params) => new URLSearchParams(Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '')).toString();

// jobTitle(BACKEND, FRONTEND, FULLSTACK)을 주면 그 직무의 현직자만 받는다.
export async function getMentors({ page = 0, size = 20, jobTitle } = {}) {
  return read(await authenticatedFetch(`${ROOT}/mentors?${query({ page, size, jobTitle })}`), { ...COMMON, default: '현직자 목록을 불러오지 못했어요.' });
}

export async function getMentor(id) {
  return read(await authenticatedFetch(`${ROOT}/mentors/${encodeURIComponent(id)}`), { ...COMMON, default: '현직자 정보를 불러오지 못했어요.' });
}

// 유료 피드백 요청(8.3). 이력서는 공통·특화 중 하나만 보낸다. 성공하면 가격만큼 크레딧이 빠진다.
export async function createPaidFeedback({ mentorId, commonResumeId, targetResumeId, content }) {
  const response = await authenticatedFetch(`${ROOT}/paid-feedback-requests`, json('POST', { mentorId, commonResumeId, targetResumeId, content }));
  return read(response, { ...COMMON, 400: '이력서를 하나 골라 주세요.', 409: '크레딧이 부족해요. 충전한 뒤 다시 요청해 주세요.', default: '요청을 보내지 못했어요.' });
}

// 내 유료 피드백 목록(8.4). 내가 요청한 것과 현직자로 받은 것이 함께 온다.
export async function getPaidFeedbacks({ status, page = 0, size = 50 } = {}) {
  return read(await authenticatedFetch(`${ROOT}/paid-feedback-requests?${query({ status, page, size })}`), { ...COMMON, default: '유료 피드백을 불러오지 못했어요.' });
}

export async function getPaidFeedback(id) {
  return read(await authenticatedFetch(`${ROOT}/paid-feedback-requests/${encodeURIComponent(id)}`), { ...COMMON, default: '요청을 불러오지 못했어요.' });
}

// 요청에 붙은 이력서. 요청자와 그 요청의 현직자만 볼 수 있다.
export async function getPaidFeedbackResume(id, signal) {
  return read(await authenticatedFetch(`${ROOT}/paid-feedback-requests/${encodeURIComponent(id)}/resume`, { signal }), { ...COMMON, default: '이력서를 불러오지 못했어요.' });
}

// 현직자 답변 등록(8.6). 첫 답변이면 요청이 완료되고 현직자에게 크레딧이 적립된다.
export async function answerPaidFeedback(id, content) {
  const response = await authenticatedFetch(`${ROOT}/paid-feedback-requests/${encodeURIComponent(id)}/responses`, json('POST', { content }));
  return read(response, { ...COMMON, 400: '답변 내용을 입력해 주세요.', 409: '취소된 요청에는 답변할 수 없어요.', default: '답변을 등록하지 못했어요.' });
}
