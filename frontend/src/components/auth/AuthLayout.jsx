import Link from 'next/link';
import Icon from '@/components/common/Icon.jsx';
import ThemeToggle from '@/components/common/ThemeToggle.jsx';
import './Auth.css';

export default function AuthLayout({ mode, children }) {
  const signup = mode === 'signup';
  return (
    <div className="auth-page">
      <a className="auth-page__skip" href="#auth-content">입력 화면으로 건너뛰기</a>
      <header className="auth-page__header">
        <Link href="/" className="auth-page__brand" aria-label="Resuit 홈">Resuit<span aria-hidden="true">.</span></Link>
        <div className="auth-page__header-actions">
          <Link href="/" className="auth-page__browse">먼저 둘러보기 <span aria-hidden="true">↗</span></Link>
          <ThemeToggle />
        </div>
      </header>
      <main className="auth-page__main">
        <section className="auth-story" aria-label="Resuit 소개">
          <div className="auth-story__eyebrow"><span /> YOUR NEXT CHAPTER</div>
          <h2>오늘의 기록이,<br />내일의 <em>가능성으로.</em></h2>
          <p className="auth-story__description">쌓아 온 개발 활동을 나만의 이력서로.<br />함께 나누는 피드백으로 다음 기회를 준비하세요.</p>
          <div className="auth-story__illustration" aria-hidden="true">
            <div className="auth-story__document">
              <div className="auth-story__document-top"><span>MY NEXT CHAPTER</span><Icon name="file" size={20} /></div>
              <div className="auth-story__document-title">꾸준함이 실력이 되도록<span>Developer · Creator · Builder</span></div>
              <div className="auth-story__activity">
                {Array.from({ length: 56 }, (_, i) => <span key={i} className={`auth-story__cell auth-story__cell--${(i * 7 + Math.floor(i / 4)) % 5}`} />)}
              </div>
              <div className="auth-story__document-bottom"><span>한 줄의 코드, 하나의 성장</span><Icon name="trend" size={20} /></div>
            </div>
            <div className="auth-story__note"><span className="auth-story__note-icon"><Icon name="check" size={18} /></span><div>경험이 쌓이는 곳<span>당신의 다음을 함께 만듭니다.</span></div></div>
          </div>
          <ol className="auth-story__steps"><li><span>01</span> 기록하고</li><li><span>02</span> 연결하고</li><li><span>03</span> 성장하세요</li></ol>
        </section>
        <section className="auth-panel" id="auth-content" aria-labelledby="auth-title" tabIndex={-1}>
          <div className="auth-panel__content">
            <div className="auth-panel__eyebrow">{signup ? 'START YOUR STORY' : 'WELCOME TO RESUIT'}</div>
            {children}
          </div>
        </section>
      </main>
      <footer className="auth-page__footer"><span>Resuit — 개발자의 경험이 기회가 되는 곳</span><span>Build your story. Shape your future.</span></footer>
    </div>
  );
}
