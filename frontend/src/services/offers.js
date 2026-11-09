import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = { 401: '로그인이 필요해요.', 403: '기업 회원만 볼 수 있어요.' };

// 응답을 JSON으로 읽고, 실패면 status를 담은 오류를 던져 화면이 로그인 안내를 구분할 수 있게 한다.
async function read(response, defaultMessage) {
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.message || FALLBACK[response.status] || defaultMessage);
    error.status = response.status;
    throw error;
  }
  return data;
}

export async function getReceivedOffers() {
  return read(await authenticatedFetch('/api/v1/users/me/offers'), '받은 제안을 불러오지 못했어요.');
}

// 우리 회사가 보낸 채용 제안(최신순). 기업 회원만 볼 수 있다.
export async function getSentOffers() {
  return read(await authenticatedFetch('/api/v1/company/offers'), '보낸 제안을 불러오지 못했어요.');
}
