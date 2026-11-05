'use client';

import { useId, useRef, useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import Button from '../common/Button.jsx';
import Avatar from '../common/Avatar.jsx';
import './Comments.css';

export default function CommentForm({ handle, initialContent = '', onSave, onCancel, inputId, disabled = false, onBusyChange }) {
  const { me, loggedIn, authLoading } = useAppState();
  const generatedId = useId();
  const id = inputId || generatedId;
  const [content, setContent] = useState(initialContent);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const pending = useRef(false);

  async function submit(event) {
    event.preventDefault();
    if (!content.trim() || pending.current || !loggedIn) return;
    pending.current = true;
    setSaving(true);
    onBusyChange?.(true);
    setError('');
    try {
      await onSave(content.trim());
      setContent('');
    } catch (err) {
      setError(err.message || '댓글을 저장하지 못했어요.');
    } finally {
      pending.current = false;
      setSaving(false);
      onBusyChange?.(false);
    }
  }

  return <form className="comment-form" onSubmit={submit} aria-busy={saving}>
    <Avatar initial={me.initial} color={me.av} size={40} />
    <div className="comment-form__body">
      <label htmlFor={id} className="small muted">{onCancel ? '댓글 수정' : `@${handle} 님에게 답글 남기기`}</label>
      <textarea id={id} rows={3} className="comment-form__input" placeholder="답글을 남겨보세요."
        value={content} disabled={saving || !loggedIn || disabled} onChange={(event) => setContent(event.target.value)} />
      {error && <p role="alert">{error}</p>}
      <div className="comment-form__footer">
        {authLoading ? <span role="status">로그인 확인 중…</span> : !loggedIn
          ? <Button to="/onboarding" variant="outline" size="sm">로그인하고 댓글 달기</Button>
          : <>
            {onCancel && <Button variant="ghost" disabled={saving} onClick={onCancel}>취소</Button>}
            <Button type="submit" disabled={saving || disabled || !content.trim()}>{saving ? '저장 중…' : onCancel ? '수정 저장' : '답글'}</Button>
          </>}
      </div>
    </div>
  </form>;
}
