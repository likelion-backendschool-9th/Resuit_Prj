'use client';

import { useEffect, useRef, useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import { createPost } from '../../services/posts.js';
import Avatar from '../common/Avatar.jsx';
import Button from '../common/Button.jsx';
import PostForm from './PostForm.jsx';
import './ComposeModal.css';

// 왼쪽 메뉴 "게시글 작성"으로 여는 글 작성 모달. 홈 상단 글쓰기와 같은 PostForm을 쓴다.
// 글을 올리면 announcePost로 알려 홈 피드가 열려 있으면 바로 보이게 한다.
export default function ComposeModal({ onClose }) {
  const { me, loggedIn, authLoading, announcePost } = useAppState();
  const dialog = useRef(null);
  const textarea = useRef(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const element = dialog.current;
    const previousFocus = document.activeElement;
    const overflow = document.body.style.overflow;
    element.showModal();
    textarea.current?.focus();
    document.body.style.overflow = 'hidden';
    return () => {
      element.close();
      document.body.style.overflow = overflow;
      previousFocus?.focus();
    };
  }, []);

  async function save(body) {
    if (!loggedIn) throw new Error('게시하려면 로그인해 주세요.');
    setSaving(true);
    try {
      announcePost(await createPost(body));
      onClose();
    } finally {
      setSaving(false);
    }
  }

  return (
    <dialog ref={dialog} className="compose-modal" aria-labelledby="compose-modal-title"
      onCancel={(event) => { event.preventDefault(); if (!saving) onClose(); }}>
      <header className="compose-modal__header">
        <h2 id="compose-modal-title">게시글 작성</h2>
        <Button variant="ghost" size="sm" disabled={saving} onClick={onClose}>닫기</Button>
      </header>
      <div className="compose-modal__body">
        <Avatar initial={me.initial} color={me.av} src={me.profileImage || undefined} />
        <div className="composer__body">
          {authLoading ? <p role="status">로그인 상태를 확인하고 있어요.</p> : <PostForm ref={textarea} onSave={save} />}
          {!authLoading && !loggedIn && <Button to="/login" variant="ghost" size="sm" onClick={onClose}>로그인하고 게시하기</Button>}
        </div>
      </div>
    </dialog>
  );
}
