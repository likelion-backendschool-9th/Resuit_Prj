import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = { 401: '로그인이 필요해요.', 404: '알림을 찾을 수 없어요.' };
const ROOT = '/api/v1/users/me/notifications';

async function read(response, defaultMessage) {
  const data = await response.json().catch(() => null);
  if (!response.ok) {
    const error = new Error(data?.message || FALLBACK[response.status] || defaultMessage);
    error.status = response.status;
    throw error;
  }
  return data;
}

// 내 알림 목록(최신순, 페이지)
export async function getNotifications({ page = 0, size = 20 } = {}) {
  return read(await authenticatedFetch(`${ROOT}?page=${page}&size=${size}`), '알림을 불러오지 못했어요.');
}

// 안 읽은 알림 수: { unread }
export async function getUnreadNotificationCount() {
  return read(await authenticatedFetch(`${ROOT}/unread-count`), '알림 수를 불러오지 못했어요.');
}

// 알림 하나를 읽음으로 바꾼다.
export async function readNotification(id) {
  return read(await authenticatedFetch(`${ROOT}/${encodeURIComponent(id)}/read`, { method: 'PATCH' }), '알림을 읽음으로 바꾸지 못했어요.');
}

// 내 알림을 모두 읽음으로 바꾼다.
export async function readAllNotifications() {
  return read(await authenticatedFetch(`${ROOT}/read-all`, { method: 'POST' }), '알림을 읽음으로 바꾸지 못했어요.');
}
