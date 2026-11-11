import { authenticatedFetch } from './auth.js';

async function request(path, options = {}) {
  const response = await authenticatedFetch(`/api/v1/reviews${path}`, { credentials: 'same-origin', ...options });
  if (!response.ok) {
    if (response.status === 401) throw new Error('로그인 후 이용할 수 있어요.');
    const error = await response.json().catch(() => null);
    throw new Error(error?.message || '리뷰를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
  }
  return response.json();
}

export const getBestReviews = (period, signal) => request(`/best?period=${period}`, { signal });
export const setReviewLiked = (id, liked) => request(`/${id}/likes`, { method: liked ? 'POST' : 'DELETE' });

export const getHallOfFame = (page, signal, size = 20) => request(`/hall-of-fame?page=${page}&size=${size}`, { signal });

export const acceptReview = (id) => request(`/${id}/accept`, { method: 'POST' });

export const getReviewHighlights = (signal) => request('/highlights', { signal });
