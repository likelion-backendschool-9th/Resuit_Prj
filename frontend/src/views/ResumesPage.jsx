'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { getAllResumes, createCommonResume, updateCommonResume, deleteCommonResume } from '../services/resumes.js';
import { createFeedbackRequest, cancelFeedbackRequest } from '../services/feedbackRequests.js';
import { MainColumn, RightAside } from '../components/layout/AppLayout.jsx';
import Tabs from '../components/common/Tabs.jsx';
import Avatar from '../components/common/Avatar.jsx';
import Badge from '../components/common/Badge.jsx';
import Button, { IconButton } from '../components/common/Button.jsx';
import Icon from '../components/common/Icon.jsx';
import SubmittedResumeImports from '../components/resume/SubmittedResumeImports.jsx';
import './ResumesPage.css';

const TABS = [{ id: 'all', label: '전체' }, { id: 'common', label: '공통' }, { id: 'special', label: '특화' }];
const STATUS = { WRITING: { label: '작성 중', tone: 'special' }, SUBMITTED: { label: '제출 완료', tone: 'accent' } };
const MAX_NAME = 100;
const FIELD_LABEL = { FRONTEND: '프론트엔드', BACKEND: '백엔드', FULLSTACK: '풀스택' };
const SECTION_LABEL = { BASIC: '기본 정보', SKILLS: '기술 스택', PROJECTS: '프로젝트', AWARDS: '수상', INTRODUCTION: '자기소개' };
const COUNTED = new Set(['PROJECTS', 'AWARDS']);

// 서버의 섹션 개수를 "기본 정보 · 기술 스택 · 프로젝트 3개" 형태로 바꾼다
function sectionText(sections) {
  if (!sections?.length) return '아직 작성한 섹션이 없어요';
  return sections.map((s) => (COUNTED.has(s.type) ? `${SECTION_LABEL[s.type]} ${s.count}개` : SECTION_LABEL[s.type])).join(' · ');
}

function updatedText(resume) {
  const verb = resume.updatedAt && resume.updatedAt !== resume.createdAt ? '수정' : '만듦';
  const minutes = Math.floor((Date.now() - new Date(resume.updatedAt || resume.createdAt).getTime()) / 60000);
  if (!(minutes >= 1)) return `방금 ${verb}`;
  if (minutes < 60) return `${minutes}분 전 ${verb}`;
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}시간 전 ${verb}`;
  return `${Math.floor(minutes / (60 * 24))}일 전 ${verb}`;
}

export default function ResumesPage() {
  const [submissions, setSubmissions] = useState([]);
  const [tab, setTab] = useState('all');
  const [commonResumes, setCommonResumes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [pending, setPending] = useState(null);
  const [retry, setRetry] = useState(0);
  const [renaming, setRenaming] = useState(null);
  const [draftName, setDraftName] = useState('');
  const [targetResumes, setTargetResumes] = useState([]);
  const [savedId, setSavedId] = useState(null);
  const writing =targetResumes.filter((r) => r.status === 'WRITING').length;
  const submitted = targetResumes.filter((r) => r.status === 'SUBMITTED').length;

  // 공통·특화 목록을 대표 API 하나로 받는다 (WBS 3.1)
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    getAllResumes(controller.signal)
      .then((all) => { setCommonResumes(all.commonResumes); setTargetResumes(all.targetResumes); setSubmissions(all.submittedResumes || []); })
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [retry]);

  // 공통 이력서 편집에서 "저장하고 나가기"로 오면(?saved=ID) 그 이력서에 저장 안내를 잠깐 띄우고 주소에서 지운다
  useEffect(() => {
    const saved = new URLSearchParams(window.location.search).get('saved');
    if (!saved) return;
    setSavedId(saved);
    window.history.replaceState(null, '', '/resumes');
  }, []);

  useEffect(() => {
    if (!savedId) return undefined;
    const timer = setTimeout(() => setSavedId(null), 5000);
    return () => clearTimeout(timer);
  }, [savedId]);

  // 작업 하나를 실행하는 동안 같은 이력서의 버튼을 막고, 실패하면 안내 문구를 보여 준다
  async function run(id, action) {
    setPending(id);
    setNotice('');
    try {
      await action();
    } catch (err) {
      setNotice(err.message);
    } finally {
      setPending(null);
    }
  }

  const startRename = (r) => { setRenaming(r.id); setDraftName(r.title); };
  const saveName = (id) => run(id, async () => {
    const title = draftName.trim();
    if (title) {
      const updated = await updateCommonResume(id, { title });
      setCommonResumes((list) => list.map((r) => (r.id === id ? { ...r, title: updated.title, updatedAt: updated.updatedAt } : r)));
    }
    setRenaming(null);
  });
  const addCommon = () => run('new', async () => {
    const created = await createCommonResume({ title: `새 공통 이력서 ${commonResumes.length + 1}` });
    setCommonResumes((list) => [...list, { ...created, sections: [], feedbackCount: 0, targetResumeCount: 0 }]);
    setRenaming(created.id);
    setDraftName('');
    if (tab === 'special') setTab('all');
  });
  const removeCommon = (id) => run(id, async () => {
    await deleteCommonResume(id);
    setCommonResumes((list) => list.filter((r) => r.id !== id));
  });
  // 피드백 요청(3.2)과 취소(3.3). 진행 중인 요청 ID로 요청 상태를 판단한다
  const toggleRequest = (r) => run(r.id, async () => {
    let requestId = null;
    if (r.feedbackRequestId) {
      try {
        await cancelFeedbackRequest(r.feedbackRequestId);
      } catch (err) {
        if (err.code === 'FEEDBACK_REQUEST_HAS_COMMENTS') {
          setCommonResumes((list) => list.map((x) => (x.id === r.id
            ? { ...x, feedbackRequestHasComments: true } : x)));
        }
        throw err;
      }
    } else requestId = (await createFeedbackRequest({ commonResumeId: r.id })).id;
    setCommonResumes((list) => list.map((x) => (x.id === r.id
      ? { ...x, feedbackRequestId: requestId, feedbackRequestHasComments: false } : x)));
  });

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
              <button type="button" className="resume-add" onClick={addCommon} disabled={loading || !!error || pending === 'new'}><Icon name="plus" size={16} />새 공통 이력서</button>
            </div>
            <p className="small muted" role="status">{notice}</p>

            {loading ? <div className="empty" role="status">이력서를 불러오는 중이에요.</div>
              : error ? <div className="empty" role="alert"><p>{error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
              : commonResumes.length === 0 ? <div className="empty">아직 공통 이력서가 없어요. 새 공통 이력서를 만들어 보세요.</div>
              : commonResumes.map((r) => {
              const requested = !!r.feedbackRequestId;
              const isPublic = r.isPublic;
              const busy = pending === r.id;
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
                            <Button size="mid" onClick={() => saveName(r.id)} disabled={busy}>저장</Button>
                            <Button variant="outline" size="mid" onClick={() => setRenaming(null)}>취소</Button>
                          </div>
                          <span className="xs muted">{draftName.length} / {MAX_NAME}자 · 지원할 때와 프로필에 이 이름으로 보여요</span>
                        </>
                      ) : (
                        <div className="row-wrap" style={{ gap: 6 }}>
                          <strong style={{ fontSize: 17 }}>{r.title}</strong>
                          <IconButton label={`「${r.title}」 이름 변경`} className="icon-btn--plain common-resume__rename" onClick={() => startRename(r)}><Icon name="pencil" size={16} /></IconButton>
                          {r.field && <Badge tone="general">{FIELD_LABEL[r.field] || r.field}</Badge>}
                          {isPublic && <Badge tone="accepted"><Icon name="eye" size={12} />프로필 공개</Badge>}
                        </div>
                      )}
                      <div className="small muted">{sectionText(r.sections)} · {updatedText(r)}</div>
                      <div className="small" style={{ color: 'var(--accent-text)', fontWeight: 500 }}>받은 피드백 {r.feedbackCount}개{r.targetResumeCount ? ` · 특화 이력서 ${r.targetResumeCount}개의 베이스` : ''}</div>
                    </div>
                    <div className="row-wrap">
                      <Button variant="outline" size="mid" to={`/resumes/common/edit?id=${r.id}`}>편집</Button>
                      <Button variant={requested ? 'outline' : 'primary'} size="mid" aria-pressed={requested} onClick={() => toggleRequest(r)} disabled={busy || (requested && r.feedbackRequestHasComments)}>
                        {requested ? '요청 취소' : '피드백 요청'}
                      </Button>
                      {requested && r.feedbackRequestHasComments && <span className="small muted">댓글이 달린 요청은 취소할 수 없어요.</span>}
                      <IconButton label={`「${r.title}」 삭제`} className="common-resume__remove" onClick={() => removeCommon(r.id)} disabled={busy}><Icon name="trash" size={16} /></IconButton>
                    </div>
                  </div>
                  {String(r.id) === savedId && (
                    <div className="request-notice" role="status">
                      <Icon name="check" size={18} />
                      <span className="request-notice__text">「{r.title}」 이력서를 저장했어요.</span>
                    </div>
                  )}
                  {requested && (
                    <div className="request-notice" role="status">
                      <Icon name="check" size={18} />
                      <span className="request-notice__text">「{r.title}」이 피드백 게시판에 올라갔어요.</span>
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
            {loading ? <div className="empty" role="status">특화 이력서를 불러오는 중이에요.</div>
              : error ? <div className="empty" role="alert"><p>{error}</p><Button variant="outline" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
              : targetResumes.length === 0 ? <div className="empty">아직 특화 이력서가 없어요. 특화 공고에서 작성을 시작해 보세요.</div>
              : targetResumes.map((r) => (
              <Link key={r.id} href={`/resumes/edit?id=${r.id}`} className="resume-row">
                <Avatar initial={(r.companyName || '?').slice(0, 1)} color="co3" company />
                <div className="grow" style={{ flexBasis: 220 }}>
                  <div className="bold" style={{ fontSize: 16 }}>{r.companyName} · {r.jobTitle}</div>
                  <div className="small muted">{r.commonResumeId ? '공통 이력서 기반' : '처음부터 작성'} · 요구 문항 {r.questionCount}개 · {updatedText(r)}</div>
                </div>
                <Badge tone={STATUS[r.status].tone} size="md">{STATUS[r.status].label}</Badge>
              </Link>
            ))}
          </section>
        )}
        {!loading && !error && <SubmittedResumeImports submissions={submissions} />}
      </MainColumn>

      <RightAside showReviews label="리뷰 추천 및 보조 정보">
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
