import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = { 401: '로그인이 필요해요.' };

// 내가 받은 채용 제안(최신순). 실패면 status를 담은 오류를 던져 화면이 로그인 안내를 구분할 수 있게 한다.
export async function getReceivedOffers() {
  const response = await authenticatedFetch('/api/v1/users/me/offers');
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.message || FALLBACK[response.status] || '받은 제안을 불러오지 못했어요.');
    error.status = response.status;
    throw error;
  }
  return data;
}
