import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = {
  400: '요청 값을 확인해 주세요.',
  401: '로그인이 필요해요.',
  403: '기업 회원만 결제할 수 있어요.',
  404: '결제를 찾을 수 없어요.',
  409: '이미 처리된 결제예요.',
  502: '결제사 응답을 확인하지 못했어요. 잠시 후 다시 시도해 주세요.',
  503: '결제 설정이 되어 있지 않아요.',
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

const post = (url, body) => authenticatedFetch(url, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(body),
});

export async function getMySubscription() {
  return read(await authenticatedFetch('/api/v1/company/subscription'), '요금제를 불러오지 못했어요.');
}

export async function createProPayment(product) {
  return read(await post('/api/v1/company/pro-payments', { product }), '주문을 만들지 못했어요.');
}

export async function confirmProPayment({ paymentKey, orderId, amount }) {
  return read(await post('/api/v1/company/pro-payments/confirm', { paymentKey, orderId, amount: Number(amount) }), '결제를 승인하지 못했어요.');
}
