import { useEffect, useState } from 'react';
import { getFeedbackComments, createFeedbackComment, adoptFeedback } from '../../services/feedbackRequests.js';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import { ChipRadioGroup } from '../common/Chip.jsx';
import { SECTION_LABEL, avatarColor, timeText } from './labels.js';

const ALL = 'ALL';
const COMMON_SECTIONS = ['BASIC', 'SKILLS', 'PROJECTS', 'AWARDS', 'INTRODUCTION'];

// 4.9 고를 수 있는 섹션: 요청자가 고른 섹션만. 고른 게 없으면 그 이력서의 모든 항목
function sectionOptions(request) {
  const sections = request.sections.length ? request.sections
    : request.resumeType === 'TARGET' ? [...COMMON_SECTIONS, 'ANSWERS'] : COMMON_SECTIONS;
  return [{ id: ALL, label: '전체' }, ...sections.map((s) => ({ id: s, label: SECTION_LABEL[s] ?? s }))];
}

// 피드백 카드를 펼쳤을 때 보이는 댓글 목록과 작성란 (WBS 4.6, 4.7, 4.9, 4.10)
export default function FeedbackComments({ request, onAdded, onAdopted }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  const [section, setSection] = useState(ALL);
  const [draft, setDraft] = useState('');
  const [sending, setSending] = useState(false);
  const [notice, setNotice] = useState('');
  const [confirming, setConfirming] = useState(null);
  const [adopting, setAdopting] = useState(false);
  const [adoptNotice, setAdoptNotice] = useState('');
  const inputId = `feedback-${request.id}`;
  const adopted = request.adopted || comments.some((c) => c.adopted);
  // 4.10 요청한 본인만, 아직 아무것도 채택하지 않았을 때 남의 피드백 하나를 채택할 수 있다
  const canAdopt = request.mine && !adopted;

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    getFeedbackComments(request.id, controller.signal)
      .then(setComments)
      .catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [request.id, retry]);

  async function submit() {
    const content = draft.trim();
    if (!content || sending) return;
    setSending(true);
    setNotice('');
    try {
      const saved = await createFeedbackComment(request.id, { section: section === ALL ? null : section, content });
      setComments((list) => [...list, saved]);
      setDraft('');
      onAdded?.();
    } catch (err) {
      setNotice(err.message);
    } finally {
      setSending(false);
    }
  }

  async function adopt(commentId) {
    setAdopting(true);
    setAdoptNotice('');
    try {
      const result = await adoptFeedback(commentId);
      setComments((list) => list.map((c) => (c.commentId === commentId ? { ...c, adopted: true, adoptedAt: result.adoptedAt } : c)));
      setConfirming(null);
      onAdopted?.();
    } catch (err) {
      setAdoptNotice(err.message);
    } finally {
      setAdopting(false);
    }
  }

  return (
    <div className="fb-card__panel">
      {request.mine && !adopted && <p className="small muted">가장 도움이 된 피드백 1개를 채택해 주세요. 채택은 리뷰어의 인증 배지 평가에 반영돼요.</p>}
      {loading ? <p className="small muted" role="status">피드백을 불러오는 중이에요.</p>
        : error ? <div role="alert" className="stack" style={{ gap: 6 }}><p className="small">{error}</p><Button variant="outline" size="sm" onClick={() => setRetry((v) => v + 1)}>다시 시도</Button></div>
        : comments.length === 0 ? <p className="small muted">아직 피드백이 없어요. 첫 피드백을 남겨 주세요.</p>
        : comments.map((c) => {
          const name = c.author.nickname || c.author.username;
          return (
            <div key={c.commentId} className="fb-item">
              <Avatar initial={name.charAt(0)} color={avatarColor(c.author.username)} size={32} src={c.author.profileImageUrl} />
              <div className="grow stack" style={{ gap: 4 }}>
                <div className="row-wrap" style={{ gap: 6, fontSize: 14 }}>
                  <strong>{name}</strong>
                  {c.reviewerBadge && <Badge tone="verified"><Icon name="check" size={11} />{c.reviewerBadge}</Badge>}
                  <Badge tone="general">{c.section ? SECTION_LABEL[c.section] ?? c.section : '전체'}</Badge>
                  <span className="xs muted">{timeText(c.createdAt)}</span>
                  {c.adopted && <span className="fb-item__adopt"><Badge tone="accepted" size="md">채택됨</Badge></span>}
                  {canAdopt && !c.mine && confirming !== c.commentId && (
                    <Button variant="accent-outline" size="sm" className="fb-item__adopt" onClick={() => { setConfirming(c.commentId); setAdoptNotice(''); }}>채택하기</Button>
                  )}
                </div>
                <p style={{ fontSize: 14, whiteSpace: 'pre-wrap' }}>{c.content}</p>
                {confirming === c.commentId && (
                  <div className="row-wrap" style={{ gap: 6 }}>
                    <span className="small">이 피드백을 채택할까요? 채택은 되돌릴 수 없어요.</span>
                    <Button size="sm" onClick={() => adopt(c.commentId)} disabled={adopting}>{adopting ? '채택 중…' : '채택'}</Button>
                    <Button variant="outline" size="sm" onClick={() => setConfirming(null)} disabled={adopting}>취소</Button>
                  </div>
                )}
                {confirming === c.commentId && adoptNotice && <p className="small" role="alert">{adoptNotice}</p>}
              </div>
            </div>
          );
        })}

      <div className="stack" style={{ gap: 8, paddingTop: 4 }}>
        <ChipRadioGroup options={sectionOptions(request)} value={section} onChange={setSection} label="피드백할 섹션" size="sm" />
        <label htmlFor={inputId} className="sr-only">피드백 작성</label>
        <textarea
          id={inputId}
          className="textarea fb-card__input"
          rows={3}
          maxLength={2000}
          placeholder="좋은 점 하나, 고칠 점 하나를 구체적으로 남겨 주세요."
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
        />
        {notice && <p className="small" role="alert">{notice}</p>}
        <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
          <Button onClick={submit} disabled={!draft.trim() || sending}>{sending ? '등록 중…' : '피드백 등록'}</Button>
        </div>
      </div>
    </div>
  );
}
