import { authenticatedFetch } from './auth.js';

async function post(path, body) {
  const response = await authenticatedFetch(`/api/v1/verifications${path}`, {
    method: 'POST', credentials: 'same-origin',
    headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || '이메일 인증 요청을 처리하지 못했어요.');
  return data;
}

export async function getMyVerificationStatus() {
  const response = await authenticatedFetch('/api/v1/verifications/me', {
    method: 'GET', credentials: 'same-origin',
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || '인증 상태를 불러오지 못했어요.');
  return data;
}

export const verifyCompanyEmail = (email) => post('/email', { email });
