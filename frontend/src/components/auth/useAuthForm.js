import { useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { loginWithPassword, signupWithPassword } from '@/services/auth.js';
import { useAppState } from '@/context/AppStateContext.jsx';

export default function useAuthForm(mode) {
  const router = useRouter();
  const { refreshSession } = useAppState();
  const pending = useRef(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [complete, setComplete] = useState(false);

  async function submit(event) {
    event.preventDefault();
    if (pending.current) return;
    const form = event.currentTarget;
    const values = Object.fromEntries(new FormData(form));
    const errors = {};
    if (mode === 'signup') {
      if (!values.nickname.trim()) errors.nickname = '닉네임을 입력해 주세요.';
      if (new TextEncoder().encode(values.password).length > 72) errors.password = '비밀번호는 한글 포함 UTF-8 기준 72바이트까지 입력할 수 있어요.';
      if (values.password !== values.confirmPassword) errors.confirmPassword = '비밀번호가 일치하지 않아요.';
    }
    setFieldErrors(errors);
    setError('');
    if (Object.keys(errors).length) {
      form.elements.namedItem(Object.keys(errors)[0])?.focus();
      return;
    }
    pending.current = true;
    setBusy(true);
    try {
      if (mode === 'signup') {
        await signupWithPassword({ username: values.username, email: values.email, nickname: values.nickname.trim(), password: values.password });
        form.reset();
        setComplete(true);
      } else {
        await loginWithPassword({ username: values.username, password: values.password });
        await refreshSession();
        router.replace('/');
      }
    } catch (failure) {
      setError(failure.message || '요청을 완료하지 못했어요. 다시 시도해 주세요.');
    } finally {
      pending.current = false;
      setBusy(false);
    }
  }
  return { submit, busy, error, fieldErrors, complete };
}
