'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { getFeed } from '../../services/posts.js';

export default function useTimeline(initialTimeline, tab, userId) {
  const key = tab === 'following' ? `following:${userId || 'guest'}` : tab;
  const enabled = tab !== 'following' || !!userId;
  const [entries, setEntries] = useState(() => ({
    recommend: { timeline: initialTimeline, error: '', loading: false },
  }));
  const cache = useRef(entries);
  const pending = useRef(null);
  const [revision, setRevision] = useState(0);
  const update = useCallback((fn) => {
    cache.current = fn(cache.current);
    setEntries(cache.current);
  }, []);

  const load = useCallback(async (reset = false) => {
    if (!enabled || pending.current) return;
    const previous = cache.current[key]?.timeline;
    if (!reset && previous?.last) return;
    const controller = new AbortController();
    pending.current = controller;
    update((all) => ({ ...all, [key]: { ...all[key], loading: true, error: '' } }));
    try {
      const next = await getFeed({ tab, userId, page: reset ? 0 : (previous?.page ?? -1) + 1, signal: controller.signal });
      if (controller.signal.aborted) return;
      const posts = new Map((reset ? [] : previous?.posts || []).map((post) => [post.id, post]));
      next.posts.forEach((post) => posts.set(post.id, post));
      update((all) => ({ ...all, [key]: { timeline: { ...next, posts: [...posts.values()] }, loading: false, error: '' } }));
    } catch (err) {
      if (!controller.signal.aborted) update((all) => ({ ...all, [key]: { ...all[key], loading: false, error: err.message } }));
    } finally {
      if (pending.current === controller) pending.current = null;
    }
  }, [enabled, key, tab, userId, update]);

  useEffect(() => {
    // 처음 여는 탭과 변경된 피드만 조회한다. 최초 추천 목록은 SSR 결과를 사용한다.
    if (enabled && (!cache.current[key]?.timeline || cache.current[key]?.stale)) load(true);
    return () => {
      if (pending.current) {
        pending.current.abort();
        pending.current = null;
        update((all) => ({ ...all, [key]: { ...all[key], loading: false, stale: true } }));
      }
    };
  }, [key, enabled, load, revision, update]);

  function invalidate() {
    pending.current?.abort();
    pending.current = null;
    update((all) => Object.fromEntries(Object.entries(all).map(([entryKey, entry]) =>
      [entryKey, { ...entry, stale: true, loading: false }])));
    setRevision((value) => value + 1);
  }

  function upsertPost(post) {
    update((all) => Object.fromEntries(Object.entries(all).map(([entryKey, entry]) => {
      if (!entry.timeline) return [entryKey, entry];
      const matches = entryKey === 'recommend' || entryKey === post.type || entryKey.startsWith('following:');
      const exists = entry.timeline.posts.some((item) => item.id === post.id);
      const posts = entry.timeline.posts.flatMap((item) => item.id === post.id ? (matches ? [post] : []) : [item]);
      if (!exists && entryKey === 'recommend') posts.unshift(post);
      return [entryKey, { ...entry, timeline: { ...entry.timeline, posts } }];
    })));
    invalidate();
  }

  function removePost(id) {
    update((all) => Object.fromEntries(Object.entries(all).map(([entryKey, entry]) =>
      [entryKey, entry.timeline ? { ...entry, timeline: { ...entry.timeline, posts: entry.timeline.posts.filter((post) => post.id !== id) } } : entry])));
    invalidate();
  }

  const entry = entries[key];
  return {
    posts: enabled ? entry?.timeline?.posts || [] : [],
    hasMore: enabled && !entry?.timeline?.last,
    loading: enabled && (entry?.loading || (!entry?.timeline && !entry?.error)),
    error: enabled ? entry?.error || '' : '',
    loadMore: () => load(!!entry?.stale), upsertPost, removePost,
  };
}
