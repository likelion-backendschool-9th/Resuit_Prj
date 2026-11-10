import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = { 400: '입력 내용을 확인해 주세요.', 401: '로그인이 필요해요.', 403: '회사 이메일로 현직자 인증을 먼저 해 주세요.' };

async function read(response, defaultMessage) {
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.message || FALLBACK[response.status] || defaultMessage);
    error.status = response.status;
    throw error;
  }
  return data;
}

// 내 현직자 인증 상태와 현직자 정보(회사명, 직무, 경력 연차, 피드백 가격)
export async function getMyMentor() {
  return read(await authenticatedFetch('/api/v1/users/me/mentor'), '현직자 정보를 불러오지 못했어요.');
}

// 현직자 정보 저장. 처음 저장하면 현직자 목록에 나온다. 회사명은 인증한 이메일로, 경력은 프로필 경력으로 정해져 보내지 않는다.
export async function saveMyMentor({ jobTitle, priceCredits }) {
  const response = await authenticatedFetch('/api/v1/users/me/mentor', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ jobTitle, priceCredits }),
  });
  return read(response, '현직자 정보를 저장하지 못했어요.');
}
