'use client';

import { useRef, useState } from 'react';
import Button from '@/components/common/Button.jsx';
import { setupPasswordLogin } from '@/services/auth.js';
import { useAppState } from '@/context/AppStateContext.jsx';

export default function PasswordSetupForm() {
  const { me, passwordLoginEnabled, refreshSession } = useAppState();
  const [password, setPassword] = useState('');
  const [confirmation, setConfirmation] = useState('');
  const [busy, setBusy] = useState(false);
  const [completed, setCompleted] = useState(false);
  const [error, setError] = useState('');
  const pending = useRef(false);

  async function submit(event) {
    event.preventDefault();
    if (pending.current) return;
    setError('');
    if (password !== confirmation) { setError('비밀번호가 일치하지 않아요.'); return; }
    if (new TextEncoder().encode(password).length > 72) { setError('비밀번호는 UTF-8 기준 72바이트까지 입력할 수 있어요.'); return; }
    pending.current = true; setBusy(true);
    try {
      await setupPasswordLogin(password);
      setPassword(''); setConfirmation(''); setCompleted(true);
      try { await refreshSession(); }
      catch { setError('비밀번호는 설정됐지만 로그인 수단 표시를 갱신하지 못했어요. 새로고침해 주세요.'); }
    } catch (failure) { setError(failure.message); }
    finally { pending.current = false; setBusy(false); }
  }

  return <section className="card integrations__card">
    <h2>아이디·비밀번호 로그인</h2>
    <p className="small">로그인 아이디: <strong>{me.handle}</strong></p>
    {passwordLoginEnabled || completed ? <p className="small muted" role="status">{completed ? '비밀번호를 설정했어요. ' : ''}위 아이디와 비밀번호로도 같은 계정에 로그인할 수 있어요.</p>
      : <><p className="small muted">새로 가입하지 않고 현재 계정에 비밀번호를 추가하세요. 기존 아이디와 이력서, 게시글은 그대로 유지됩니다.</p>
        <form className="stack" onSubmit={submit} aria-busy={busy}>
          <input type="text" name="username" value={me.handle} autoComplete="username" readOnly hidden />
          <div className="field"><label htmlFor="setup-password">새 비밀번호</label><input id="setup-password" className="input" type="password" autoComplete="new-password" required minLength={8} maxLength={72} value={password} onChange={(event) => setPassword(event.target.value)} disabled={busy} aria-describedby="setup-password-hint" /></div>
          <p className="xs muted" id="setup-password-hint">8자 이상, UTF-8 기준 최대 72바이트</p>
          <div className="field"><label htmlFor="setup-password-confirm">새 비밀번호 확인</label><input id="setup-password-confirm" className="input" type="password" autoComplete="new-password" required minLength={8} maxLength={72} value={confirmation} onChange={(event) => setConfirmation(event.target.value)} disabled={busy} /></div>
          <Button type="submit" disabled={busy}>{busy ? '설정 중…' : '비밀번호 로그인 추가'}</Button>
        </form></>}
    {error && <p role="alert" className="small">{error}</p>}
  </section>;
}
