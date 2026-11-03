'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { SPECIAL_RESUMES } from '../data/mock.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button, { IconButton } from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import './ResumesPage.css';

const TABS = [{ id: 'all', label: '전체' }, { id: 'common', label: '공통' }, { id: 'special', label: '특화' }];
const STATUS = { writing: { label: '작성 중', tone: 'special' }, submitted: { label: '제출 완료', tone: 'accent' } };
const MAX_NAME = 40;

export default function ResumesPage() {
  const { commonResumes, addCommonResume, renameCommonResume, removeCommonResume, publicResumeIds, isResumeRequested, toggleResumeRequest } = useAppState();
  const [tab, setTab] = useState('all');
  const [renaming, setRenaming] = useState(null);
  const [draftName, setDraftName] = useState('');
  const writing = SPECIAL_RESUMES.filter((r) => r.status === 'writing').length;
  const submitted = SPECIAL_RESUMES.filter((r) => r.status === 'submitted').length;

  const startRename = (r) => { setRenaming(r.id); setDraftName(r.name); };
  const saveName = (id) => {
    const name = draftName.trim();
    if (name) renameCommonResume(id, name);
    setRenaming(null);
  };
  const addCommon = () => {
    setRenaming(addCommonResume());
    setDraftName('');
    if (tab === 'special') setTab('all');
  };

  return (
    <>
      <MainColumn>
        <div className="page-title" style={{ alignItems: 'center', paddingBottom: 0 }}>
          <h1>내 이력서</h1>
          <Button variant="dark" to="/jobs">새 특화 이력서</Button>
        </div>
        <div style={{ marginTop: 8 }}><Tabs items={TABS} value={tab} onChange={setTab} label="이력서 종류" /></div>

        {tab !== 'special' && (
          <section className="section">
            <div className="row-wrap" style={{ justifyContent: 'space-between', gap: 12 }}>
              <h2 className="section-label">공통 이력서 {commonResumes.length}개 · 일반 공고에 그대로 제출</h2>
              <button type="button" className="resume-add" onClick={addCommon}><Icon name="plus" size={16} />새 공통 이력서</button>
            </div>

            {commonResumes.map((r) => {
              const requested = isResumeRequested(r.id);
              const isPublic = publicResumeIds.includes(r.id);
              return (
                <article key={r.id} className={`common-resume${isPublic ? ' is-public' : ''}`}>
                  <div className="common-resume__row">
                    <div className="resume-thumb common-resume__thumb" aria-hidden="true"><span /><span /><span /><span /></div>
                    <div className="common-resume__info">
                      {renaming === r.id ? (
                        <>
                          <div className="row-wrap" style={{ gap: 6 }}>
                            <label htmlFor={`resume-name-${r.id}`} className="sr-only">이력서 이름</label>
                            <input
                              id={`resume-name-${r.id}`}
                              className="common-resume__name-input"
                              maxLength={MAX_NAME}
                              placeholder="예: 백엔드 지원용"
                              value={draftName}
                              onChange={(e) => setDraftName(e.target.value)}
                            />
                            <Button size="mid" onClick={() => saveName(r.id)}>저장</Button>
                            <Button variant="outline" size="mid" onClick={() => setRenaming(null)}>취소</Button>
                          </div>
                          <span className="xs muted">{draftName.length} / {MAX_NAME}자 · 지원할 때와 프로필에 이 이름으로 보여요</span>
                        </>
                      ) : (
                        <div className="row-wrap" style={{ gap: 6 }}>
                          <strong style={{ fontSize: 17 }}>{r.name}</strong>
                          <IconButton label={`「${r.name}」 이름 변경`} className="icon-btn--plain common-resume__rename" onClick={() => startRename(r)}><Icon name="pencil" size={16} /></IconButton>
                          <Badge tone="general">{r.field}</Badge>
                          {isPublic && <Badge tone="accepted"><Icon name="eye" size={12} />프로필 공개</Badge>}
                        </div>
                      )}
                      <div className="small muted">{r.sections} · {r.updated}</div>
                      <div className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>받은 피드백 {r.feedback}개{r.bases ? ` · 특화 이력서 ${r.bases}개의 베이스` : ''}</div>
                    </div>
                    <div className="row-wrap">
                      <Button variant="outline" size="mid" to="/resumes/edit?job=1&mode=base">편집</Button>
                      <Button variant={requested ? 'outline' : 'primary'} size="mid" aria-pressed={requested} onClick={() => toggleResumeRequest(r)}>
                        {requested ? '요청 취소' : '피드백 요청'}
                      </Button>
                      <IconButton label={`「${r.name}」 삭제`} className="common-resume__remove" onClick={() => removeCommonResume(r.id)}><Icon name="trash" size={16} /></IconButton>
                    </div>
                  </div>
                  {requested && (
                    <div className="request-notice" role="status">
                      <Icon name="check" size={18} />
                      <span className="request-notice__text">「{r.name}」이 피드백 게시판에 올라갔어요.</span>
                      <Link href="/feedback" className="text-link" style={{ fontSize: 13 }}>피드백 보러 가기</Link>
                    </div>
                  )}
                </article>
              );
            })}

            <div className="resume-tip">
              <Icon name="info" size={16} />
              <span className="grow" style={{ flexBasis: 200 }}>프로필에 어떤 이력서를 공개할지는 프로필에서 정해요.</span>
              <Link href="/profile" className="text-link" style={{ fontSize: 13 }}>프로필 공개 설정</Link>
            </div>
          </section>
        )}

        {tab !== 'common' && (
          <section className="section">
            <h2 className="section-label">특화 이력서 · 공고별로 작성</h2>
            {SPECIAL_RESUMES.map((r) => (
              <Link key={r.id} href={`/resumes/edit?job=${r.jobId || 1}&mode=base`} className="resume-row">
                <Avatar initial={r.initial} color="co3" company />
                <div className="grow" style={{ flexBasis: 220 }}>
                  <div className="bold" style={{ fontSize: 16 }}>{r.company} · {r.role}</div>
                  <div className="small muted">{r.base} · 요구 문항 {r.questions}개 · {r.updated}</div>
                </div>
                <Badge tone={STATUS[r.status].tone} size="md">{STATUS[r.status].label}</Badge>
              </Link>
            ))}
          </section>
        )}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>지원 현황</h2>
          <div className="grid-3" style={{ gap: 8 }}>
            <div className="stat"><b>{writing}</b><span className="xs muted">작성 중</span></div>
            <div className="stat"><b>{submitted}</b><span className="xs muted">제출 완료</span></div>
            <div className="stat"><b>0</b><span className="xs muted">결과 발표</span></div>
          </div>
        </section>
        <section className="card" style={{ gap: 8 }}>
          <h2>이력서 구조</h2>
          <p className="text-2" style={{ fontSize: 14, lineHeight: 1.7 }}>공통 이력서는 용도별로 여러 개 만들고 이름을 붙여 관리해요. 특화 이력서는 공고마다 하나씩, 원하는 공통 이력서를 복사해 시작하거나 빈 상태로 시작합니다.</p>
        </section>
      </RightAside>
    </>
  );
}
