'use client';

import { useRef, useState } from 'react';
import { updatePost, deletePost } from '../../services/posts.js';
import Button from '../common/Button.jsx';
import PostForm from './PostForm.jsx';

export default function PostOwnerActions({ post, onUpdated, onDeleted }) {
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

  if (editing) return <PostForm post={post} onCancel={() => setEditing(false)} onSave={async (body) => {
    const saved = await updatePost(post.id, body);
    onUpdated(saved);
    setEditing(false);
  }} />;

  return <div className="post-owner-actions" aria-busy={deleting}>
    {confirming ? <>
      <p>이 게시글을 삭제할까요? 삭제하면 되돌릴 수 없어요.</p>
      <div className="row">
        <Button variant="outline" disabled={deleting} onClick={remove}>{deleting ? '삭제 중…' : '삭제 확인'}</Button>
        <Button variant="ghost" disabled={deleting} onClick={() => { setConfirming(false); setError(''); }}>취소</Button>
      </div>
    </> : <div className="row">
      <Button variant="ghost" size="sm" onClick={() => setEditing(true)}>수정</Button>
      <Button variant="ghost" size="sm" onClick={() => setConfirming(true)}>삭제</Button>
    </div>}
    {error && <p role="alert">{error}</p>}
  </div>;
}
