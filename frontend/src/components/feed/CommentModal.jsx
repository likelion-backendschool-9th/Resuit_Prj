'use client';

import { useEffect, useRef, useState } from 'react';
import { createComment } from '../../services/comments.js';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import Button from '../common/Button.jsx';
import PostContent from './PostContent.jsx';
import CommentForm from './CommentForm.jsx';
import './Comments.css';

export default function CommentModal({ post, onClose }) {
  const { me, loggedIn } = useAppState();
  const dialog = useRef(null);
  const [saving, setSaving] = useState(false);
  const avatarSrc = (loggedIn && post.handle === me.handle && me.profileImage) || post.profileImage || undefined;
  useEffect(() => {
    const element = dialog.current;
    const previousFocus = document.activeElement;
    const overflow = document.body.style.overflow;
    element.showModal();
    document.body.style.overflow = 'hidden';
    return () => {
      element.close();
      document.body.style.overflow = overflow;
      previousFocus?.focus();
    };
  }, []);

  return <dialog ref={dialog} className="comment-modal" aria-labelledby={`comment-modal-${post.id}`}
    onCancel={(event) => { event.preventDefault(); if (!saving) onClose(); }} onClick={(event) => event.stopPropagation()}>
    <header className="comment-modal__header">
      <h2 id={`comment-modal-${post.id}`}>답글 작성</h2>
      <Button variant="ghost" size="sm" disabled={saving} onClick={() => onClose()}>닫기</Button>
    </header>
    <div className="comment-modal__post">
      <Avatar initial={post.initial} src={avatarSrc} size={40} />
      <div className="comment-form__body">
        <div><strong>{post.author}</strong> <span className="small muted">@{post.handle}</span></div>
        <div className="comment-modal__text"><PostContent content={post.body || '첨부 게시글'} /></div>
      </div>
    </div>
    <CommentForm onBusyChange={setSaving} handle={post.handle} onSave={async (content) => {
      await createComment(post.id, content);
      onClose(true);
    }} />
  </dialog>;
}
