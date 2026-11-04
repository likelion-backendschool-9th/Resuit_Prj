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
export const sendVerificationEmail = (email) => post('/email', { email });
export const confirmVerificationEmail = (verificationId, code) => post('/email/confirm', { verificationId, code });
