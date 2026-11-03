import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button, { IconButton } from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import './CompanyPostPage.css';

const TYPES = [
  { id: 'general', title: '일반 공고', desc: '지원자가 공통 이력서를 그대로 제출' },
  { id: 'special', title: '특화 공고', desc: '원하는 자기소개 문항을 받아 특화 이력서로 지원' }
];
const NEW_QUESTION_LIMIT = 500;

// 회사 계정: 공고 작성 (특화 공고면 요구 문항 편집)
export default function CompanyPostPage() {
  const navigate = useNavigate();
  const [type, setType] = useState('special');
  const [title, setTitle] = useState('프론트엔드 인턴 (React · TypeScript)');
  const [questions, setQuestions] = useState([
    { id: 1, text: '가장 어려웠던 UI 문제와 해결 과정을 알려주세요.', limit: 500 },
    { id: 2, text: '블루웨일 서비스 중 하나를 골라 개선점을 제안해 주세요.', limit: 800 }
  ]);
  const special = type === 'special';

  const updateQ = (id, text) => setQuestions((qs) => qs.map((q) => (q.id === id ? { ...q, text } : q)));

  return (
    <>
      <MainColumn>
        <div className="page-title" style={{ alignItems: 'center', padding: '16px 24px', borderBottom: '1px solid var(--line)' }}>
          <h1>새 채용 공고</h1>
          <span className="small muted">블루웨일 스튜디오 계정으로 게시</span>
        </div>

        <div className="stack" style={{ padding: 24, gap: 22 }}>
          <fieldset className="company-post__fieldset">
            <legend className="bold">공고 유형</legend>
            <div className="grid-2" role="radiogroup" aria-label="공고 유형">
              {TYPES.map((t) => (
                <button key={t.id} type="button" role="radio" aria-checked={type === t.id} className={`choice${type === t.id ? ' is-on' : ''}`} onClick={() => setType(t.id)}>
                  <span className="choice__title">{t.title}</span>
                  <span className="choice__desc">{t.desc}</span>
                </button>
              ))}
            </div>
          </fieldset>

          <div className="field"><label htmlFor="cp-title">공고 제목</label><input id="cp-title" className="input" value={title} onChange={(e) => setTitle(e.target.value)} /></div>
          <div className="grid-2">
            <div className="field"><label htmlFor="cp-emp">고용 형태</label><input id="cp-emp" className="input" defaultValue="인턴 3개월" /></div>
            <div className="field"><label htmlFor="cp-deadline">마감일</label><input id="cp-deadline" className="input" type="date" /></div>
          </div>
          <div className="field"><label htmlFor="cp-desc">상세 내용</label><textarea id="cp-desc" className="textarea" rows={5} placeholder="하는 일, 자격 요건, 우대 사항을 적어 주세요." /></div>

          {special && (
            <section className="q-box">
              <div className="row-wrap" style={{ justifyContent: 'space-between', alignItems: 'baseline', gap: 12 }}>
                <h2 style={{ fontSize: 17, fontWeight: 900 }}>요구 문항</h2>
                <span className="small muted">지원자의 특화 이력서에 그대로 들어갑니다</span>
              </div>
              {questions.map((q, i) => (
                <div key={q.id} className="q-item">
                  <span className="q-item__num">{i + 1}</span>
                  <label className="sr-only" htmlFor={`q-${q.id}`}>문항 {i + 1}</label>
                  <input id={`q-${q.id}`} className="q-item__text" value={q.text} placeholder="[새 문항을 입력하세요]" onChange={(e) => updateQ(q.id, e.target.value)} />
                  <span className="small muted">{q.limit}자 이내</span>
                  <IconButton label="문항 삭제" className="icon-btn--plain icon-btn--sm q-item__remove" onClick={() => setQuestions((qs) => qs.filter((x) => x.id !== q.id))}>
                    <Icon name="x" size={16} />
                  </IconButton>
                </div>
              ))}
              <button type="button" className="q-box__add" onClick={() => setQuestions((qs) => [...qs, { id: Date.now(), text: '', limit: NEW_QUESTION_LIMIT }])}>문항 추가</button>
            </section>
          )}

          <div className="row" style={{ justifyContent: 'flex-end' }}>
            <Button variant="outline" className="company-post__action">임시 저장</Button>
            <Button className="company-post__action" onClick={() => navigate('/')}>피드에 게시</Button>
          </div>
        </div>
      </MainColumn>

      <RightAside>
        <h2 style={{ fontSize: 17, fontWeight: 900 }}>피드 미리보기</h2>
        <div className="card" style={{ gap: 10, padding: 14 }}>
          <div className="row"><Avatar initial="B" color="co2" size={36} company /><strong style={{ fontSize: 14 }}>블루웨일 스튜디오</strong></div>
          <span style={{ alignSelf: 'flex-start' }}>
            <Badge tone={special ? 'special' : 'general'} size="md">{special ? `특화 공고 · 요구 문항 ${questions.length}개` : '일반 공고'}</Badge>
          </span>
          <div className="bold">{title || '[공고 제목]'}</div>
          <div className="small muted">{special ? '지원자는 공고에서 특화 이력서를 작성해 제출합니다.' : '지원자는 공통 이력서로 바로 지원합니다.'}</div>
        </div>
      </RightAside>
    </>
  );
}
