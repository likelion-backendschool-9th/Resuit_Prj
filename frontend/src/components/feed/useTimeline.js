'use client';

import { useRef, useState } from 'react';
import { getPosts } from '../../services/posts.js';

export default function useTimeline(initialTimeline) {
  const [timeline, setTimeline] = useState(initialTimeline);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(initialTimeline ? '' : '게시글을 불러오지 못했어요. 다시 시도해 주세요.');
  const pending = useRef(false);
  const revision = useRef(0);

  async function loadMore() {
    if (pending.current || timeline?.last) return;
    pending.current = true;
    const requestRevision = revision.current;
    setLoading(true);
    setError('');
    try {
      const next = await getPosts({ page: timeline ? timeline.page + 1 : 0 });
      if (requestRevision !== revision.current) return;
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

  function upsertPost(post) {
    revision.current += 1;
    setTimeline((previous) => ({
      ...(previous || { page: -1, last: false }),
      posts: previous?.posts.some((item) => item.id === post.id)
        ? previous.posts.map((item) => item.id === post.id ? post : item)
        : [post, ...(previous?.posts || [])],
    }));
  }

  function removePost(id) {
    revision.current += 1;
    setTimeline((previous) => previous && ({ ...previous,
      // 삭제로 페이지 경계가 당겨지므로 다음 요청에서 한 페이지를 겹쳐 읽는다.
      page: Math.max(-1, previous.page - 1),
      posts: previous.posts.filter((post) => post.id !== id),
    }));
  }

  return { upsertPost, removePost, posts: timeline?.posts || [], hasMore: !timeline?.last, loading, error, loadMore };
}
