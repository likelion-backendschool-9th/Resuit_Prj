'use client';

import { useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useAppState } from '../context/AppStateContext.jsx';
import { resumeApi } from '../services/resumeApi.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import Badge from '../components/common/Badge.jsx';
import Button, { IconButton } from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import './ResumesPage.css';

const TABS = [{ id: 'all', label: '전체' }, { id: 'common', label: '공통' }, { id: 'special', label: '특화' }];
const MAX_NAME = 40;

function fieldLabel(field) {
  if (field === 'FRONTEND') return '프론트엔드';
  if (field === 'BACKEND') return '백엔드';
  return field || '미정';
}

function formatUpdated(value) {
  if (!value) return '방금 저장';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString('ko-KR', {
    month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

function contentSummary(content) {
  if (!content || typeof content !== 'object') return '아직 작성한 섹션이 없어요';
  const keys = Object.keys(content);
  if (keys.length === 0) return '아직 작성한 섹션이 없어요';
  return keys.slice(0, 4).join(' · ');
}

export default function ResumesPage() {
  const { me, loggedIn, authLoading, isResumeRequested, toggleResumeRequest } = useAppState();
  const [tab, setTab] = useState('all');
  const [commonResumes, setCommonResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [renaming, setRenaming] = useState(null);
  const [draftName, setDraftName] = useState('');

  const load = useCallback(async () => {
    if (!loggedIn) {
      setCommonResumes([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    try {
      const items = await resumeApi.listCommon(me.handle);
      setCommonResumes(items);
      setError('');
    } catch {
      setError('공통 이력서를 불러오지 못했습니다.');
      setCommonResumes([]);
    } finally {
      setLoading(false);
    }
  }, [loggedIn, me.handle]);

  useEffect(() => {
    if (authLoading) return undefined;
    load();
    return undefined;
  }, [authLoading, load]);

  const startRename = (r) => { setRenaming(r.id); setDraftName(r.title || ''); };
  const saveName = async (id) => {
    const title = draftName.trim();
    if (!title) return;
    try {
      const updated = await resumeApi.updateCommon(me.handle, id, { title });
      setCommonResumes((list) => list.map((r) => (r.id === id ? updated : r)));
      setRenaming(null);
      setError('');
    } catch {
      setError('이름을 저장하지 못했습니다.');
    }
  };

  const addCommon = async () => {
    try {
      const created = await resumeApi.createCommon(me.handle, {
        title: `새 공통 이력서 ${commonResumes.length + 1}`,
        field: 'FRONTEND',
        content: {},
      });
      setCommonResumes((list) => [created, ...list]);
      setRenaming(created.id);
      setDraftName(created.title);
      if (tab === 'special') setTab('all');
      setError('');
    } catch {
      setError('공통 이력서를 만들지 못했습니다.');
    }
  };

  const removeCommon = async (id, title) => {
    if (!window.confirm(`「${title}」을(를) 삭제할까요?`)) return;
    try {
      await resumeApi.deleteCommon(me.handle, id);
      setCommonResumes((list) => list.filter((r) => r.id !== id));
      setError('');
    } catch {
      setError('삭제하지 못했습니다. 지원·피드백에 연결된 이력서일 수 있어요.');
    }
  };

  return (
    <>
      <MainColumn>
        <div className="page-title" style={{ alignItems: 'center', paddingBottom: 0 }}>
          <h1>내 이력서</h1>
          <Button variant="dark" to="/jobs">새 특화 이력서</Button>
        </div>
        <div style={{ marginTop: 8 }}><Tabs items={TABS} value={tab} onChange={setTab} label="이력서 종류" /></div>

        {!authLoading && !loggedIn && (
          <p className="small muted" style={{ margin: '16px 0' }}>로그인하면 내 공통 이력서를 관리할 수 있어요.</p>
        )}
        {error && <p className="small muted" role="alert" style={{ margin: '12px 0' }}>{error}</p>}
        {loading && loggedIn && <p className="small muted" style={{ margin: '12px 0' }}>불러오는 중…</p>}

        {tab !== 'special' && loggedIn && (
          <section className="section">
            <div className="row-wrap" style={{ justifyContent: 'space-between', gap: 12 }}>
              <h2 className="section-label">공통 이력서 {commonResumes.length}개 · 일반 공고에 그대로 제출</h2>
              <button type="button" className="resume-add" onClick={addCommon}>
                <Icon name="plus" size={16} />
                새 공통 이력서
              </button>
            </div>

            {commonResumes.length === 0 && !loading && (
              <p className="small muted">아직 공통 이력서가 없어요. 새 공통 이력서로 시작해 보세요.</p>
            )}

            {commonResumes.map((r) => {
              const requested = isResumeRequested(r.id);
              const title = r.title;
              return (
                <article key={r.id} className={`common-resume${r.isPublic ? ' is-public' : ''}`}>
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
                          <strong style={{ fontSize: 17 }}>{title}</strong>
                          <IconButton label={`「${title}」 이름 변경`} className="icon-btn--plain common-resume__rename" onClick={() => startRename(r)}>
                            <Icon name="pencil" size={16} />
                          </IconButton>
                          <Badge tone="general">{fieldLabel(r.field)}</Badge>
                          {r.isPublic && (
                            <Badge tone="accepted">
                              <Icon name="eye" size={12} />
                              프로필 공개
                            </Badge>
                          )}
                        </div>
                      )}
                      <div className="small muted">{contentSummary(r.content)} · {formatUpdated(r.updatedAt)}</div>
                    </div>
                    <div className="row-wrap">
                      <Button variant="outline" size="mid" to={`/resumes/edit?common=${r.id}`}>편집</Button>
                      <Button
                        variant={requested ? 'outline' : 'primary'}
                        size="mid"
                        aria-pressed={requested}
                        onClick={() => toggleResumeRequest({
                          id: r.id,
                          name: title,
                          field: fieldLabel(r.field),
                          sections: contentSummary(r.content),
                        })}
                      >
                        {requested ? '요청 취소' : '피드백 요청'}
                      </Button>
                      <IconButton
                        label={`「${title}」 삭제`}
                        className="common-resume__remove"
                        onClick={() => removeCommon(r.id, title)}
                      >
                        <Icon name="trash" size={16} />
                      </IconButton>
                    </div>
                  </div>
                  {requested && (
                    <div className="request-notice" role="status">
                      <Icon name="check" size={18} />
                      <span className="request-notice__text">「{title}」이 피드백 게시판에 올라갔어요.</span>
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
            <p className="small muted">
              특화 이력서 API는 아직 연결 전이에요. 공고에서 지원을 시작하면 여기에 표시될 예정입니다.
            </p>
            <Button variant="outline" to="/jobs">공고 보러 가기</Button>
          </section>
        )}
      </MainColumn>

      <RightAside>
        <section className="card">
          <h2>지원 현황</h2>
          <div className="grid-3" style={{ gap: 8 }}>
            <div className="stat"><b>{commonResumes.length}</b><span className="xs muted">공통</span></div>
            <div className="stat"><b>0</b><span className="xs muted">특화</span></div>
            <div className="stat"><b>0</b><span className="xs muted">결과 발표</span></div>
          </div>
        </section>
        <section className="card" style={{ gap: 8 }}>
          <h2>이력서 구조</h2>
          <p className="text-2" style={{ fontSize: 14, lineHeight: 1.7 }}>
            공통 이력서는 용도별로 여러 개 만들고 이름을 붙여 관리해요. 특화 이력서는 공고마다 하나씩, 원하는 공통 이력서를 복사해 시작하거나 빈 상태로 시작합니다.
          </p>
        </section>
      </RightAside>
    </>
  );
}
