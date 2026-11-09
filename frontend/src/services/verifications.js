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

// 등록된 회사 도메인 이메일을 보내면 서버가 바로 현직자 인증을 끝낸다. 회원 역할은 바뀌지 않는다. 코드 확인 단계는 없다.
export const verifyCompanyEmail = (email) => post('/email', { email });

export async function cancelCompanyEmailVerification() {
  const response = await authenticatedFetch('/api/v1/verifications/email', {
    method: 'DELETE', credentials: 'same-origin',
  });
  const data = await response.json().catch(() => null);
  if (!response.ok) throw new Error(data?.message || '현직자 인증을 취소하지 못했어요.');
  return data;
}
