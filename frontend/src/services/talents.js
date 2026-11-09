import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = {
  400: '요청 값을 확인해 주세요.',
  401: '로그인이 필요해요.',
  403: '기업 회원만 쓸 수 있어요.',
  404: '공개 이력서를 찾을 수 없어요.',
  409: '기업 Pro 체험은 한 번만 할 수 있어요.',
};

async function read(response, defaultMessage) {
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.message || FALLBACK[response.status] || defaultMessage);
    error.status = response.status;
    throw error;
  }
  return data;
}

const json = (method, body) => ({ method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });

export async function getTalents(query = {}) {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return;
    if (Array.isArray(value)) {
      if (value.length) params.set(key, value.join(','));
      return;
    }
    params.set(key, String(value));
  });
  const response = await authenticatedFetch(`/api/v1/company/talents?${params}`);
  return read(response, '인재 목록을 불러오지 못했어요.');
}

export async function getTalentResume(userId, resumeId) {
  const response = await authenticatedFetch(`/api/v1/company/talents/${encodeURIComponent(userId)}/resumes/${encodeURIComponent(resumeId)}`);
  return read(response, '이력서를 불러오지 못했어요.');
}

// 채용 제안 보내기(9.4). 공고 선택은 아직 없어서 메시지만 보낸다.
export async function sendCompanyOffer({ userId, message }) {
  const response = await authenticatedFetch('/api/v1/company/offers', json('POST', { userId, message }));
  return read(response, '채용 제안을 보내지 못했어요.');
}

export async function getCompanySubscription() {
  const response = await authenticatedFetch('/api/v1/company/subscription');
  return read(response, '요금제를 불러오지 못했어요.');
}

// 기업 Pro 14일 체험 시작. 체험은 회사마다 한 번만 된다.
export async function startProTrial() {
  const response = await authenticatedFetch('/api/v1/company/subscription', json('PUT', { plan: 'TRIAL' }));
  return read(response, '체험을 시작하지 못했어요.');
}
