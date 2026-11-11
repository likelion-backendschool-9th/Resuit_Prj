import { authenticatedFetch } from './auth.js';

async function request(id, method = 'GET', body, signal) {
  const response = await authenticatedFetch(`/api/v1/posts/${encodeURIComponent(id)}/comments`, {
    method, signal, cache: 'no-store',
    ...(body ? { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) } : {}),
  });
  if (!response.ok) {
    if (response.status === 401) throw new Error('로그인이 필요해요. 다시 로그인해 주세요.');
    if (response.status === 403) throw new Error('권한이 없거나 인증이 만료되었어요. 새로고침 후 다시 시도해 주세요.');
    throw new Error('댓글 요청을 처리하지 못했어요. 다시 시도해 주세요.');
  }
  return method === 'DELETE' ? null : response.json();
}

export const getComments = (postId, signal) => request(postId, 'GET', undefined, signal);
export const createComment = (postId, content, parentId = null) => request(postId, 'POST', { content, parentId });
// 현재 백엔드 계약: PATCH/DELETE의 경로 id는 게시글이 아닌 댓글 ID다.
export const updateComment = (commentId, content) => request(commentId, 'PATCH', { content });
export const deleteComment = (commentId) => request(commentId, 'DELETE');
