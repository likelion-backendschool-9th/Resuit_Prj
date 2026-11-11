import { authenticatedFetch } from './auth.js';

// 상태 코드별 기본 안내 문구. 배포 환경에서는 서버 오류 메시지가 응답에 담기지 않을 수 있어 따로 둔다.
const FALLBACK = {
  400: '요청 값을 확인해 주세요.',
  401: '로그인이 필요해요.',
  404: '결제를 찾을 수 없어요.',
  409: '이미 처리된 결제예요.',
  502: '결제사 응답을 확인하지 못했어요. 잠시 후 다시 시도해 주세요.',
  503: '결제 설정이 되어 있지 않아요.',
};

async function read(response, defaultMessage) {
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || FALLBACK[response.status] || defaultMessage);
  return data;
}

export async function getCreditBalance() {
  const response = await authenticatedFetch('/api/v1/credits/balance');
  return read(response, '잔액을 불러오지 못했어요.');
}

export async function getCreditTransactions(size = 10) {
  const response = await authenticatedFetch(`/api/v1/credits/transactions?page=0&size=${size}`);
  return read(response, '크레딧 내역을 불러오지 못했어요.');
}

export async function createCreditPayment(credits) {
  const response = await authenticatedFetch('/api/v1/credit-payments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ credits }),
  });
  return read(response, '충전 주문을 만들지 못했어요.');
}

export async function confirmCreditPayment({ paymentKey, orderId, amount }) {
  const response = await authenticatedFetch('/api/v1/credit-payments/confirm', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ paymentKey, orderId, amount: Number(amount) }),
  });
  return read(response, '결제를 승인하지 못했어요.');
}

// 적립 크레딧을 환전한다. 성공하면 환전 결과와 바뀐 잔액(balance, withdrawable)이 온다.
export async function withdrawCredits({ credits, bankName, accountNumber, accountHolder }) {
  const response = await authenticatedFetch('/api/v1/credits/withdrawals', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ credits, bankName, accountNumber, accountHolder }),
  });
  if (response.status === 409) {
    const data = await response.json().catch(() => null);
    throw new Error(data?.message || '환전할 수 있는 크레딧이 부족해요.');
  }
  return read(response, '환전하지 못했어요.');
}
