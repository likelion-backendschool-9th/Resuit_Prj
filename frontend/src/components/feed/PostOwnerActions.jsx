'use client';

import { useEffect, useId, useRef, useState } from 'react';
import { updatePost, deletePost } from '../../services/posts.js';
import Button from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import './PostOwnerActions.css';
import PostForm from './PostForm.jsx';

export default function PostOwnerActions({ post, onUpdated, onDeleted }) {
  const [open, setOpen] = useState(false);
  const root = useRef(null);
  const trigger = useRef(null);
  const panelId = useId();
  useEffect(() => {
    if (!open) return;
    function outside(event) {
      if (!root.current?.contains(event.target) && !pending.current) {
        setOpen(false); setConfirming(false); setError('');
      }
    }
    function escape(event) {
      if (event.key === 'Escape' && !pending.current) {
        setOpen(false); setConfirming(false); setError(''); trigger.current?.focus();
      }
    }
    document.addEventListener('pointerdown', outside);
    document.addEventListener('keydown', escape);
    return () => {
      document.removeEventListener('pointerdown', outside);
      document.removeEventListener('keydown', escape);
    };
  }, [open]);
  const [editing, setEditing] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const pending = useRef(false);

  async function remove() {
    if (pending.current) return;
    pending.current = true;
    setDeleting(true);
    setError('');
    try {
      await deletePost(post.id);
      onDeleted(post.id);
    } catch (err) {
      setError(err.message || '삭제하지 못했어요. 다시 시도해 주세요.');
    } finally {
      pending.current = false;
      setDeleting(false);
    }
  }

  if (editing) return <div className="post-owner-editor" onClick={(event) => event.stopPropagation()}><PostForm post={post} onCancel={() => setEditing(false)} onSave={async (body) => {
    const saved = await updatePost(post.id, body);
    onUpdated(saved);
    setEditing(false);
  }} /></div>;

  return <div ref={root} className="post-owner-actions" aria-busy={deleting} onClick={(event) => event.stopPropagation()}>
    <button ref={trigger} type="button" className="icon-btn icon-btn--plain" aria-label="게시글 관리"
      aria-expanded={open} aria-controls={panelId} disabled={deleting}
      onClick={() => { setOpen((value) => !value); setConfirming(false); setError(''); }}>
      <Icon name="more" />
    </button>
    {open && <div id={panelId} className="post-owner-actions__panel">
    {confirming ? <>
      <p>이 게시글을 삭제할까요? 삭제하면 되돌릴 수 없어요.</p>
      <div className="row">
        <Button className="post-owner-actions__delete" variant="outline" disabled={deleting} onClick={remove}>{deleting ? '삭제 중…' : '삭제 확인'}</Button>
        <Button variant="ghost" disabled={deleting} onClick={() => { setConfirming(false); setError(''); }}>취소</Button>
      </div>
    </> : <div className="post-owner-actions__items">
      <Button variant="ghost" size="sm" onClick={() => { setOpen(false); setEditing(true); }}><Icon name="pencil" size={16} />게시글 수정</Button>
      <Button className="post-owner-actions__delete" variant="ghost" size="sm" onClick={() => setConfirming(true)}><Icon name="trash" size={16} />게시글 삭제</Button>
    </div>}
    {error && <p role="alert">{error}</p>}
    </div>}
  </div>;
}
