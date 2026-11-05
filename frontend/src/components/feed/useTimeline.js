'use client';

import { useRef, useState } from 'react';
import { getPosts } from '../../services/posts.js';

export default function useTimeline(initialTimeline) {
  const [timeline, setTimeline] = useState(initialTimeline);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(initialTimeline ? '' : '게시글을 불러오지 못했어요. 다시 시도해 주세요.');
  const pending = useRef(false);

  async function loadMore() {
    if (pending.current || timeline?.last) return;
    pending.current = true;
    setLoading(true);
    setError('');
    try {
      const next = await getPosts({ page: timeline ? timeline.page + 1 : 0 });
      setTimeline((previous) => {
        const posts = new Map((previous?.posts || []).map((post) => [post.id, post]));
        next.posts.forEach((post) => posts.set(post.id, post));
        return { ...next, posts: [...posts.values()] };
      });
    } catch {
      setError('게시글을 불러오지 못했어요. 다시 시도해 주세요.');
    } finally {
      pending.current = false;
      setLoading(false);
    }
  }

  return { posts: timeline?.posts || [], hasMore: !timeline?.last, loading, error, loadMore };
}
