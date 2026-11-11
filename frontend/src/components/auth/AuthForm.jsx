import { useEffect, useRef, useState } from 'react';
import Link from 'next/link';
import Button from '@/components/common/Button.jsx';
import Icon from '@/components/common/Icon.jsx';
import { ChipRadioGroup } from '@/components/common/Chip.jsx';
import AuthField from './AuthField.jsx';
import useAuthForm from './useAuthForm.js';
import { useAppState } from '@/context/AppStateContext.jsx';

const PROVIDER_NAMES = { google: 'Google', github: 'GitHub', naver: '네이버' };

export default function AuthForm({ mode }) {
  const signup = mode === 'signup';
  const { submit, busy, error, fieldErrors, complete } = useAuthForm(mode);
  const { loggedIn, me, authProviders, authLoading } = useAppState();
  const [oauthError, setOauthError] = useState(false);
  // 가입 종류. 기업(COMPANY)이면 기업 회원으로 가입하고 닉네임 칸을 회사명으로 쓴다.
  const [accountType, setAccountType] = useState('USER');
  const company = signup && accountType === 'COMPANY';
  const successHeading = useRef(null);
  useEffect(() => {
    setOauthError(new URLSearchParams(window.location.search).get('error') === 'oauth');
  }, []);
  useEffect(() => { if (complete) successHeading.current?.focus(); }, [complete]);

  if (complete || loggedIn) return (
    <div className="auth-panel__success">
      <span className="auth-panel__success-icon"><Icon name="check" size={28} /></span>
      <h1 id="auth-title" ref={successHeading} tabIndex={-1}>{complete ? '첫걸음을 환영해요.' : `${me.name}님, 반가워요.`}</h1>
      <p>{complete ? '회원가입이 완료되었어요. 새로 만든 계정으로 로그인하고 나만의 이야기를 시작하세요.' : '이미 로그인되어 있어요. 오늘의 기록과 새로운 기회를 만나 보세요.'}</p>
      <Button to={complete ? '/login' : '/'} size="lg" block>{complete ? '로그인하러 가기' : '피드로 이동하기'} <span aria-hidden="true">→</span></Button>
    </div>
  );

  const providers = authProviders.filter((provider) => provider.enabled && PROVIDER_NAMES[provider.id]);
  return (
    <>
      <h1 id="auth-title">{signup ? '가능성의 시작, 회원가입' : '다시 만나 반가워요.'}</h1>
      <p className="auth-panel__description">{signup ? '당신의 경험이 빛날 수 있도록, Resuit과 함께하세요.' : '오늘의 기록을 이어가고, 다음 기회를 만나세요.'}</p>
      <form className="auth-form" onSubmit={submit} aria-busy={busy}>
        <fieldset disabled={busy}>
          <AuthField name="username" label="아이디" placeholder="아이디를 입력해 주세요" required autoComplete="username" autoCapitalize="none" spellCheck={false}
            minLength={signup ? 3 : undefined} maxLength={50} pattern={signup ? '[a-zA-Z0-9_\\-]+' : undefined}
            title={signup ? '영문, 숫자, 밑줄 또는 하이픈 3~50자' : undefined} hint={signup ? '영문, 숫자, 밑줄(_), 하이픈(-) 3~50자' : undefined} />
          {signup && <div className="auth-form__type">
            <span className="auth-form__type-label">가입 종류</span>
            <ChipRadioGroup label="가입 종류" value={accountType} onChange={setAccountType}
              options={[{ id: 'USER', label: '개인 회원' }, { id: 'COMPANY', label: '기업 회원' }]} />
            <input type="hidden" name="accountType" value={accountType} />
            {company && <p className="auth-form__type-hint">기업 회원은 인재 찾기, 채용 제안, 공고 관리를 쓸 수 있어요. 회사명은 프로필 이름으로 보여요.</p>}
          </div>}
          {signup && <div className="auth-form__row">
            <AuthField key={accountType} name="nickname" label={company ? '회사명' : '닉네임'} placeholder={company ? '예: 뉴렉처' : '어떻게 불러드릴까요?'} required maxLength={50} autoComplete={company ? 'organization' : 'nickname'} error={fieldErrors.nickname} />
          </div>}
          {signup && <AuthField name="email" label="이메일" placeholder="you@example.com" type="email" required maxLength={255} autoComplete="email" autoCapitalize="none" spellCheck={false} />}
          <AuthField name="password" label="비밀번호" placeholder={signup ? '8자 이상의 비밀번호' : '비밀번호를 입력해 주세요'} type="password" required
            minLength={signup ? 8 : undefined} maxLength={72} autoComplete={signup ? 'new-password' : 'current-password'}
            hint={signup ? '8자 이상 · 최대 72바이트 (영문·숫자 기준 72자)' : undefined} error={fieldErrors.password} />
          {signup && <AuthField name="confirmPassword" label="비밀번호 확인" placeholder="비밀번호를 한 번 더 입력해 주세요" type="password" required maxLength={72} autoComplete="new-password" error={fieldErrors.confirmPassword} />}
        </fieldset>
        {(error || oauthError) && <div className="auth-form__error" role="alert"><Icon name="info" size={18} /><p>{error || '소셜 로그인을 완료하지 못했어요. 다시 시도해 주세요.'}</p></div>}
        <Button type="submit" size="lg" block disabled={busy || authLoading}>
          {busy ? (signup ? '계정을 만들고 있어요…' : '로그인 중…') : authLoading ? '로그인 상태 확인 중…' : signup ? 'Resuit 시작하기' : '로그인'}
          {!busy && !authLoading && <span aria-hidden="true">→</span>}
        </Button>
      </form>
      {!signup && providers.length > 0 && <div className="auth-social">
        <div className="auth-social__divider"><span>또는 소셜 계정으로</span></div>
        <div className="auth-social__options">{providers.map((provider) => <a key={provider.id} href={provider.loginUrl} className="btn btn--outline btn--md">
          {provider.id === 'github' && <Icon name="github" size={18} />}{PROVIDER_NAMES[provider.id]}
        </a>)}</div>
      </div>}
      <p className="auth-panel__switch">{signup ? '이미 계정이 있으신가요?' : '아직 Resuit 회원이 아니신가요?'} <Link href={signup ? '/login' : '/signup'}>{signup ? '로그인' : '회원가입'}</Link></p>
      <div className="auth-panel__footnote"><Icon name="lock" size={14} /><span>당신의 다음 도전을 위한, 나만의 공간</span></div>
    </>
  );
}
