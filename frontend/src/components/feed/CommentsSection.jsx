'use client';

import { useCallback, useEffect, useId, useRef, useState } from 'react';
import { getComments, createComment, updateComment, deleteComment } from '../../services/comments.js';
import { useAppState } from '../../context/AppStateContext.jsx';
import Icon from '../common/Icon.jsx';
import Avatar from '../common/Avatar.jsx';
import Badge from '../common/Badge.jsx';
import Button from '../common/Button.jsx';
import CommentForm from './CommentForm.jsx';
import './Comments.css';

function CommentItem({ comment, postId, parentHandle, onReplied, onUpdated, onDeleted }) {
  const { me, loggedIn } = useAppState();
  const [editing, setEditing] = useState(false);
  const [replying, setReplying] = useState(false);
  const [replySaving, setReplySaving] = useState(false);
  const replyId = useId();
  const [confirming, setConfirming] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');
  const pending = useRef(false);
  const author = comment.author.nickname || comment.author.username;
  async function remove() {
    if (pending.current) return;
    pending.current = true;
    setDeleting(true);
    setError('');
    try {
      await deleteComment(comment.id);
      onDeleted(comment.id);
    } catch (err) { setError(err.message); }
    finally { pending.current = false; setDeleting(false); }
  }
  return <>
    <article className={`comment-item${replying || comment.replies?.length ? ' comment-item--connected' : ''}`}>
      <Avatar initial={author.slice(0, 1)} src={comment.author.profileImageUrl} size={40} />
      <div className="comment-form__body">
        <header className="comment-item__header"><strong>{author}</strong>
          {comment.author.displayedBadgeName && (
            <Badge tone="sharer" size="sm">{comment.author.displayedBadgeName}</Badge>
          )}
          <span className="small muted">@{comment.author.username} · {comment.createdAt?.replace('T', ' ').slice(0, 16)}</span>
        </header>
        {parentHandle && <span className="comment-item__reply-target">@{parentHandle} 님에게 보내는 답글</span>}
        {editing ? <CommentForm mode="edit" autoFocus initialContent={comment.content} onCancel={() => setEditing(false)} onSave={async (content) => {
          const saved = await updateComment(comment.id, content);
          onUpdated(saved);
          setEditing(false);
        }} /> : <p className="comment-item__text">{comment.content}</p>}
        {!editing && <div className="comment-item__reply-actions">
          <button type="button" className="comment-item__reply-button" aria-label={`${author}님에게 답글 달기`}
            aria-expanded={replying} aria-controls={replying ? replyId : undefined} disabled={deleting || replySaving}
            onClick={() => { setReplying((value) => !value); setConfirming(false); }}>
            <Icon name="msg" size={18} /><span>{comment.replies?.length || '답글'}</span>
          </button>
        </div>}
        {!editing && loggedIn && me.handle === comment.author.username && <div className="comment-item__controls">
          {confirming ? <>
            <span className="small">댓글을 삭제할까요?</span>
            <Button variant="outline" size="sm" disabled={deleting} onClick={remove}>{deleting ? '삭제 중…' : '삭제 확인'}</Button>
            <Button variant="ghost" size="sm" disabled={deleting} onClick={() => { setConfirming(false); setError(''); }}>취소</Button>
          </> : <>
            <Button variant="ghost" size="sm" disabled={replySaving} onClick={() => { setReplying(false); setEditing(true); }}>수정</Button>
            <Button variant="ghost" size="sm" disabled={replySaving} onClick={() => { setReplying(false); setConfirming(true); }}>삭제</Button>
          </>}
        </div>}
        {error && <p role="alert">{error}</p>}
      </div>
    </article>
    {replying && <div id={replyId} className="comment-item__reply-composer">
      <CommentForm handle={comment.author.username} autoFocus onBusyChange={setReplySaving}
        onCancel={() => setReplying(false)} onSave={async (content) => {
          const saved = await createComment(postId, content, comment.id);
          onReplied(comment.id, saved);
          setReplying(false);
        }} />
    </div>}
    {comment.replies?.length > 0 && <div className="comment-item__replies">
      {comment.replies.map((reply) => <CommentItem key={reply.id} comment={reply} postId={postId} parentHandle={comment.author.username} onReplied={onReplied} onUpdated={onUpdated} onDeleted={onDeleted} />)}
    </div>}
  </>;
}

function addReply(items, parentId, saved) {
  return items.map((item) => item.id === parentId
    ? { ...item, replies: [...(item.replies || []), saved] }
    : { ...item, replies: addReply(item.replies || [], parentId, saved) });
}

function editTree(items, saved) {
  return items.map((item) => item.id === saved.id ? { ...saved, replies: item.replies }
    : { ...item, replies: editTree(item.replies || [], saved) });
}
function removeFromTree(items, id) {
  // 서버는 삭제된 부모의 살아 있는 답글을 상위 목록에 표시한다.
  return items.flatMap((item) => item.id === id ? removeFromTree(item.replies || [], id)
    : [{ ...item, replies: removeFromTree(item.replies || [], id) }]);
}

function countComments(items) {
  return items.reduce((total, item) => total + 1 + countComments(item.replies || []), 0);
}

export default function CommentsSection({ post, onCountChange }) {
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [retry, setRetry] = useState(0);
  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError('');
    getComments(post.id, controller.signal).then((items) => {
      if (!controller.signal.aborted) {
        setComments(items);
        onCountChange?.(countComments(items));
      }
    }).catch((err) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [post.id, retry]);
  const onReplied = useCallback((parentId, saved) => {
    setComments((items) => addReply(items, parentId, saved));
    onCountChange?.((count) => count + 1);
  }, [onCountChange]);
  const onUpdated = useCallback((saved) => setComments((items) => editTree(items, saved)), []);
  const onDeleted = useCallback((id) => {
    setComments((items) => removeFromTree(items, id));
    onCountChange?.((count) => Math.max(0, count - 1));
  }, [onCountChange]);
  return <>
    <CommentForm inputId="thread-reply" handle={post.handle} disabled={loading} onSave={async (content) => {
      const saved = await createComment(post.id, content);
      setComments((items) => [...items, saved]);
      onCountChange?.((count) => count + 1);
    }} />
    <section aria-labelledby="thread-comments-heading" aria-busy={loading}>
      <h2 id="thread-comments-heading" className="thread-comments__heading">댓글</h2>
      {loading && <p className="empty" role="status">댓글을 불러오는 중…</p>}
      {error && <div className="empty"><p role="alert">{error}</p><Button variant="outline" onClick={() => setRetry((value) => value + 1)}>다시 시도</Button></div>}
      {!loading && !error && comments.length === 0 && <p className="empty">첫 댓글을 남겨보세요.</p>}
      {comments.map((comment) => <CommentItem key={comment.id} comment={comment} postId={post.id} onReplied={onReplied} onUpdated={onUpdated} onDeleted={onDeleted} />)}
    </section>
  </>;
}
