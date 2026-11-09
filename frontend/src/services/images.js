import { authenticatedFetch } from './auth.js';

// 이미지 업로드(POST /api/v1/media/images). 게시글 업로드와 저장 방식은 같지만 이미지만 받는다.
// 응답 path(tmpImage/...)를 이력서 저장 요청에 넣으면 서버가 정식 폴더로 옮긴다.
export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp';
export const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

export async function uploadImage(file) {
  if (!file || !IMAGE_ACCEPT.split(',').includes(file.type)) throw new Error('JPG, PNG, GIF, WebP 이미지만 올릴 수 있어요.');
  if (file.size > MAX_IMAGE_BYTES) throw new Error('사진은 5MB 이하로 골라 주세요.');
  const body = new FormData();
  body.append('file', file);
  const response = await authenticatedFetch('/api/v1/media/images', { method: 'POST', body });
  if (!response.ok) {
    if (response.status === 401) throw new Error('로그인한 뒤 사진을 올릴 수 있어요.');
    if (response.status === 413) throw new Error('사진 용량이 너무 커요.');
    throw new Error('사진을 올리지 못했어요. 다시 시도해 주세요.');
  }
  return response.json();
}

// 저장하지 않고 바꾼 임시 사진을 지운다. 실패해도 화면 동작에는 영향이 없어 오류를 무시한다.
export async function discardImage(path) {
  if (!path || !path.startsWith('tmpImage/')) return;
  await authenticatedFetch(`/api/v1/media/images?${new URLSearchParams({ path })}`, { method: 'DELETE' }).catch(() => {});
}
