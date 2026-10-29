import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAppState } from '../context/AppStateContext.jsx';
import { SHARER_BADGES } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import '../components/feed/PostCard.css';

export default function ThreadPage() {
  const { id } = useParams();
  const { me, threads, addAnswer } = useAppState();
  const thread = threads[id] || threads[1];
  const [draft, setDraft] = useState('');
  const [helpful, setHelpful] = useState({});

  const submit = () => {
    if (!draft.trim()) return;
    addAnswer(thread.id, draft.trim());
    setDraft('');
  };

  return (
    <>
      <MainColumn>
        <div className="page-head">
          <Link to="/" className="icon-btn icon-btn--plain" aria-label="뒤로"><Icon name="back" /></Link>
          <h1>게시글</h1>
        </div>

        <article className="section">
          <div className="row" style={{ gap: 12 }}>
            <Avatar initial={thread.initial} color={thread.av} size={48} />
            <div className="grow"><div className="bold">{thread.author}</div><div className="small muted">@{thread.handle}</div></div>
            <Badge tone="question" size="md">질문</Badge>
          </div>
          <p style={{ fontSize: 20 }}>{thread.body}</p>
          <div className="row-wrap">{thread.tags.map((t) => <span key={t} className="small" style={{ color: 'var(--accent-text)' }}>#{t}</span>)}</div>
          <div className="small muted">{thread.time}</div>
          <div className="row muted" style={{ gap: 20, padding: '12px 0', borderTop: '1px solid var(--line)', fontSize: 14 }}>
            <span><strong style={{ color: 'var(--text)' }}>{thread.answers.length}</strong> 답변</span>
            <span><strong style={{ color: 'var(--text)' }}>{thread.reposts}</strong> 리포스트</span>
            <span><strong style={{ color: 'var(--text)' }}>{thread.likes}</strong> 좋아요</span>
          </div>
        </article>

        <div className="post-card">
          <Avatar initial={me.initial} color={me.av} size={40} />
          <div className="post-card__body">
            <label htmlFor="answer" className="small muted">@{thread.handle} 님에게 답변</label>
            <textarea
              id="answer" rows={2} placeholder="경험을 바탕으로 답변을 남겨 주세요." value={draft} onChange={(e) => setDraft(e.target.value)}
              style={{ border: 0, outline: 'none', background: 'none', resize: 'none', fontSize: 17 }}
            />
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}><Button onClick={submit} disabled={!draft.trim()}>답변</Button></div>
          </div>
        </div>

        {thread.answers.map((a) => {
          const on = !!helpful[a.id];
          return (
            <article key={a.id} className="post-card" style={{ padding: '16px 24px' }}>
              <Avatar initial={a.initial} color={a.av} size={40} company={a.company} />
              <div className="post-card__body" style={{ gap: 6 }}>
                <header className="post-card__head">
                  <strong>{a.author}</strong>
                  {a.company && <Badge tone="company">기업</Badge>}
                  {SHARER_BADGES[a.handle] && <Badge tone="sharer">{SHARER_BADGES[a.handle]}</Badge>}
                  <span className="muted">@{a.handle} · {a.time}</span>
                  {a.accepted && <Badge tone="accepted">작성자가 채택</Badge>}
                </header>
                <p className="post-card__text">{a.body}</p>
                <div className="post-card__actions" style={{ gap: 24 }}>
                  <button
                    type="button" className="post-card__action" aria-pressed={on}
                    onClick={() => setHelpful((m) => ({ ...m, [a.id]: !m[a.id] }))}
                    style={on ? { color: 'var(--accent-text)', fontWeight: 700 } : undefined}
                  >
                    <Icon name="thumb" size={17} />도움돼요 {a.likes + (on ? 1 : 0)}
                  </button>
                  <button type="button" className="post-card__action">답글</button>
                </div>
              </div>
            </article>
          );
        })}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>질문 작성자</h2>
          <Link to="/profile" className="bold" style={{ textDecoration: 'none' }}>{thread.author} @{thread.handle}</Link>
          <span className="small muted">컴퓨터공학 4학년 · 백엔드 지망</span>
        </section>
      </RightAside>
    </>
  );
}
