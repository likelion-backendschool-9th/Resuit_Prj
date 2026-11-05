'use client';

import { useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAppState } from '../../context/AppStateContext.jsx';
import { likePost, unlikePost } from '../../services/posts.js';
import Icon from '../common/Icon.jsx';
import './LikeButton.css';

export default function LikeButton({ post }) {
  const { loggedIn, authLoading, me, liked, setPostLiked } = useAppState();
  const router = useRouter();
  const key = `${me.handle}:${post.id}`;
  const active = loggedIn && (liked[key] ?? post.liked ?? false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const pending = useRef(false);
  async function toggle() {
    if (pending.current) return;
    if (!loggedIn) { router.push('/onboarding'); return; }
    pending.current = true;
    setBusy(true);
    setError('');
    try {
      await (active ? unlikePost(post.id) : likePost(post.id));
      setPostLiked(key, !active);
    } catch (err) { setError(err.message); }
    finally { pending.current = false; setBusy(false); }
  }
  return <div className="post-like" onClick={(event) => event.stopPropagation()}>
    <button type="button" className={`post-like__button${active ? ' is-liked' : ''}`} onClick={toggle}
      disabled={busy || authLoading} aria-busy={busy} aria-pressed={active}
      aria-label={active ? '좋아요 취소' : '좋아요'} title={active ? '좋아요 취소' : '좋아요'}>
      <Icon name="heart" filled={active} size={20} />
    </button>
    {error && <p className="post-like__error" role="alert">{error}</p>}
  </div>;
}
