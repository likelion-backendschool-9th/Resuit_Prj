import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAppState } from '../context/AppStateContext.jsx';
import { REPOS, RSS_RULES } from '../data/mock.js';
import Avatar from '../components/common/Avatar.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import ThemeToggle from '../components/common/ThemeToggle.jsx';
import { ChipRadioGroup } from '../components/common/Chip.jsx';
import './OnboardingPage.css';

const STEPS = ['GitHub 로그인', '가져올 정보', '블로그 연결'];
const MAX_REPOS = 3;
const PLATFORMS = Object.entries(RSS_RULES).map(([id, r]) => ({ id, label: r.label }));

// 시작하기 = 로그인 화면. 로그아웃하면 여기로 온다.
export default function OnboardingPage() {
  const { me, loggedIn, login } = useAppState();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [picked, setPicked] = useState(['study-match-web', 'button-kit']);
  const [platform, setPlatform] = useState('velog');
  const [blogId, setBlogId] = useState('seoyeon');
  const rule = RSS_RULES[platform];

  const toggleRepo = (name) =>
    setPicked((list) => (list.includes(name) ? list.filter((x) => x !== name) : list.length < MAX_REPOS ? [...list, name] : list));

  const finish = () => {
    login();
    navigate('/');
  };

  return (
    <div className="onboarding">
      <div className="onboarding__wrap">
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}><ThemeToggle withLabel /></div>

        <div className="onboarding__hero">
          <div className="onboarding__brand">Resset</div>
          <p className="muted">개발 활동이 곧 이력서가 되는 곳.<br />GitHub과 블로그를 연결하고 피드백을 받아 보세요.</p>
          {!loggedIn && <p className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>로그아웃되었어요. 다시 로그인해 주세요.</p>}
        </div>

        <ol className="onboarding__steps" aria-label="가입 단계">
          {STEPS.map((s, i) => (
            <li key={s} className={`${i < step ? 'is-done' : ''} ${i === step - 1 ? 'is-current' : ''}`}>
              <span className="onboarding__bar" />{s}
            </li>
          ))}
        </ol>

        {step === 1 && (
          <section className="onboarding__panel">
            <h1>GitHub으로 시작하기</h1>
            <p className="muted">가입과 동시에 GitHub 계정이 연결돼요. 따로 입력하지 않아도 기여 기록과 레포를 프로필과 이력서에 가져올 수 있어요.</p>
            <Button variant="dark" size="lg" block onClick={() => setStep(2)}><Icon name="github" />GitHub 계정으로 계속하기</Button>
            <div className="onboarding__divider">또는</div>
            <Button variant="outline" size="lg" block>이메일로 가입 (기업 회원)</Button>
            <p className="xs muted">공개 프로필, 공개 레포, 기여 기록만 읽어요. 코드를 수정하거나 비공개 레포에 접근하지 않아요.</p>
          </section>
        )}

        {step === 2 && (
          <section className="onboarding__panel">
            <div className="row" style={{ gap: 12 }}>
              <Avatar initial={me.initial} color={me.av} size={52} />
              <div><div className="bold" style={{ fontSize: 17 }}>{me.github} 연결됨</div><div className="small" style={{ color: 'var(--accent-text)' }}>GitHub 계정 인증 완료</div></div>
            </div>
            <h1>무엇을 가져올까요?</h1>
            <label className="check onboarding__box"><input type="checkbox" defaultChecked /> 기여 기록 (잔디)을 프로필에 표시</label>
            <fieldset className="onboarding__box onboarding__fieldset">
              <legend>대표 레포 선택 (최대 {MAX_REPOS}개)</legend>
              {REPOS.map((r) => {
                const on = picked.includes(r.name);
                return (
                  <label key={r.name} className="check">
                    <input type="checkbox" checked={on} disabled={!on && picked.length >= MAX_REPOS} onChange={() => toggleRepo(r.name)} />
                    <span><strong>{r.name}</strong> <span className="muted">· {r.lang}</span></span>
                  </label>
                );
              })}
              <span className="xs muted">{picked.length} / {MAX_REPOS}개 선택</span>
            </fieldset>
            <Button size="lg" block onClick={() => setStep(3)}>다음 · 블로그 연결</Button>
          </section>
        )}

        {step === 3 && (
          <section className="onboarding__panel">
            <h1>기술 블로그 연결</h1>
            <p className="muted">새 글을 쓰면 Resset 피드에 자동으로 공유되고, 이력서 프로젝트의 근거로 붙일 수 있어요. 블로그의 RSS 주소로 연결합니다.</p>
            <ChipRadioGroup options={PLATFORMS} value={platform} onChange={setPlatform} label="블로그 플랫폼" />
            <div className="field">
              <label htmlFor="blog-id">{rule.input}</label>
              <input id="blog-id" className="input" value={blogId} onChange={(e) => setBlogId(e.target.value)} placeholder={platform === 'etc' ? 'https://내블로그주소/rss' : '아이디만 입력하세요'} />
              <span className="small muted" style={{ wordBreak: 'break-all' }}>RSS 주소: {blogId.trim() ? rule.url(blogId.trim()) : '[아이디를 입력하세요]'}</span>
            </div>
            {blogId.trim() && (
              <div className="onboarding__preview">
                <span className="xs bold" style={{ color: 'var(--accent-text)' }}>미리보기 · 최근 글</span>
                <span className="bold">디자인 시스템 버튼 컴포넌트를 만들며 배운 것</span>
                <span className="small muted">연결하면 이후 새 글부터 피드에 공유돼요</span>
              </div>
            )}
            <Button size="lg" block onClick={finish}>연결하고 피드로 가기</Button>
            <button type="button" className="btn btn--ghost btn--md" style={{ alignSelf: 'center' }} onClick={finish}>나중에 할게요</button>
          </section>
        )}

        {step > 1 && <Button variant="ghost" style={{ alignSelf: 'center' }} onClick={() => setStep(step - 1)}>이전 단계</Button>}
        {loggedIn && <Link to="/" className="small muted" style={{ alignSelf: 'center' }}>홈으로 돌아가기</Link>}
      </div>
    </div>
  );
}
