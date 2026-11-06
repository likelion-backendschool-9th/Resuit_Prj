import { authenticatedFetch } from './auth.js';

export const MAX_MEDIA_BYTES = 50 * 1024 * 1024;
export const MEDIA_ACCEPT = '.jpg,.jpeg,.png,.gif,.webp,.mp4,.webm,.mov,.m4v';

export function mediaKind(file) {
  const extension = file.name.split('.').pop().toLowerCase();
  if (file.type.startsWith('image/') && ['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(extension)) return 'IMAGE';
  if (file.type.startsWith('video/') && ['mp4', 'webm', 'mov', 'm4v'].includes(extension)) return 'VIDEO';
  throw new Error('JPG, PNG, GIF, WebP 이미지 또는 MP4, WebM, MOV, M4V 영상을 선택해 주세요.');
}

async function check(response) {
  if (response.ok) return;
  if (response.status === 401) throw new Error('파일을 첨부하려면 로그인해 주세요.');
  if (response.status === 413) throw new Error('파일은 50MB 이하로 선택해 주세요.');
  throw new Error('파일 요청을 처리하지 못했어요. 다시 시도해 주세요.');
}

export async function uploadMedia(file) {
  const body = new FormData();
  body.append('file', file);
  const response = await authenticatedFetch('/api/v1/posts/uploads', { method: 'POST', body });
  await check(response);
  return response.json();
}

export async function discardMedia(path) {
  const response = await authenticatedFetch(`/api/v1/posts/uploads?${new URLSearchParams({ path })}`, { method: 'DELETE' });
  await check(response);
}
