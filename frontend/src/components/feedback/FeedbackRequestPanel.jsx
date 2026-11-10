'use client';

import { useState } from 'react';
import Button from '../common/Button.jsx';
import { ChipRadioGroup, ToggleChip } from '../common/Chip.jsx';
import { createFeedbackRequest, cancelFeedbackRequest } from '../../services/feedbackRequests.js';
import { SECTION_LABEL } from './labels.js';
import './FeedbackRequestPanel.css';

const MAX_MESSAGE = 1000;
const SECTIONS = ['BASIC', 'SKILLS', 'PROJECTS', 'AWARDS', 'INTRODUCTION', 'ANSWERS'];
const FIELD_OPTIONS = [{ id: 'FRONTEND', label: '프론트엔드' }, { id: 'BACKEND', label: '백엔드' }, { id: '', label: '정하지 않음' }];

// 특화 이력서 작성 화면의 "제출 전 피드백 받기" (WBS 3.2 요청, 3.3 취소).
// requestId·hasComments: 진행 중인 요청 상태. 요청·취소가 끝나면 onChange로 새 상태를 알린다.
// beforeRequest: 요청 전에 작성 중인 내용을 저장한다. 실패하면 false를 돌려준다.
export default function FeedbackRequestPanel({ resumeId, requestId, hasComments, beforeRequest, onChange }) {
  const [sections, setSections] = useState([]);
  const [category, setCategory] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState('');

  const toggleSection = (type) => setSections((list) => (list.includes(type) ? list.filter((t) => t !== type) : [...list, type]));

  async function run(task) {
    setBusy(true);
    setNotice('');
    try {
      await task();
    } catch (err) {
      setNotice(err.message);
    } finally {
      setBusy(false);
    }
  }

  // 미리보기는 저장된 내용을 보여 주므로 먼저 저장하고 요청한다
  const request = () => run(async () => {
    if (beforeRequest && !(await beforeRequest())) {
      setNotice('저장하지 못해 요청하지 않았어요. 다시 시도해 주세요.');
      return;
    }
    const created = await createFeedbackRequest({
      targetResumeId: resumeId,
      sections: SECTIONS.filter((type) => sections.includes(type)),
      category: category || undefined,
      content: message.trim() || undefined
    });
    onChange({ requestId: created.id, hasComments: false });
  });

  const cancel = () => run(async () => {
    try {
      await cancelFeedbackRequest(requestId);
      onChange({ requestId: null, hasComments: false });
    } catch (err) {
      if (err.code === 'FEEDBACK_REQUEST_HAS_COMMENTS') onChange({ requestId, hasComments: true });
      throw err;
    }
  });

  if (requestId) {
    return (
      <section className="card fb-request" aria-live="polite">
        <h2 className="fb-request__title">제출 전 피드백 받기</h2>
        <p className="fb-request__state is-on">{hasComments ? '피드백이 달렸어요. 게시판에서 확인해 보세요.' : '피드백 게시판에 요청 중이에요.'}</p>
        <div className="row-wrap" style={{ gap: 8 }}>
          <Button variant="accent-outline" size="sm" to="/feedback">게시판에서 보기</Button>
          {!hasComments && <Button variant="outline" size="sm" onClick={cancel} disabled={busy}>요청 취소</Button>}
        </div>
        {hasComments && <p className="xs muted">댓글이 달린 요청은 취소할 수 없어요.</p>}
        {notice && <p className="small" role="alert">{notice}</p>}
      </section>
    );
  }

  return (
    <section className="card fb-request">
      <h2 className="fb-request__title">제출 전 피드백 받기</h2>
      <p className="text-2 fb-request__desc">회사명을 가린 채 피드백 게시판에 올려 다른 사용자에게 피드백을 받을 수 있어요.</p>

      <div className="fb-request__group">
        <span className="small fb-request__label">봐 줬으면 하는 항목</span>
        <div className="chips">
          {SECTIONS.map((type) => (
            <ToggleChip key={type} size="sm" on={sections.includes(type)} onClick={() => toggleSection(type)}>{SECTION_LABEL[type]}</ToggleChip>
          ))}
        </div>
        <span className="xs muted">고르지 않으면 이력서 전체에 대한 피드백을 받아요.</span>
      </div>

      <div className="fb-request__group">
        <span className="small fb-request__label">분야</span>
        <ChipRadioGroup options={FIELD_OPTIONS} value={category} onChange={setCategory} label="피드백 분야" size="sm" />
      </div>

      <div className="fb-request__group">
        <label htmlFor={`fb-request-message-${resumeId}`} className="small fb-request__label">요청 메시지 (선택)</label>
        <textarea
          id={`fb-request-message-${resumeId}`}
          className="textarea"
          rows={3}
          maxLength={MAX_MESSAGE}
          placeholder="예: 지원 동기 문항이 회사와 잘 맞는지 봐 주세요."
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          disabled={busy}
        />
        <span className="xs muted fb-request__count">{message.length} / {MAX_MESSAGE}자</span>
      </div>

      <Button variant="accent-outline" onClick={request} disabled={busy}>{busy ? '요청하는 중…' : '저장하고 피드백 요청'}</Button>
      {notice && <p className="small" role="alert">{notice}</p>}
    </section>
  );
}
