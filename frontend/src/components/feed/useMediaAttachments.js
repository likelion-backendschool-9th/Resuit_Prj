'use client';

import { useEffect, useRef, useState } from 'react';
import { discardMedia, uploadMedia, mediaKind, MAX_MEDIA_BYTES } from '../../services/media.js';

export default function useMediaAttachments() {
  const [items, setItems] = useState([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const current = useRef([]);
  const pending = useRef(false);
  const mounted = useRef(false);
  const publishing = useRef(false);
  const assign = (next) => { current.current = next; if (mounted.current) setItems(next); };

  useEffect(() => {
    mounted.current = true;
    return () => {
      mounted.current = false;
      current.current.forEach((item) => {
        URL.revokeObjectURL(item.preview);
        if (!publishing.current) discardMedia(item.path).catch(() => {});
      });
    };
  }, []);

  async function add(file) {
    if (!file || pending.current || publishing.current) return;
    setError('');
    try {
      const kind = mediaKind(file);
      if (!file.size) throw new Error('빈 파일은 첨부할 수 없어요.');
      if (file.size > MAX_MEDIA_BYTES) throw new Error('파일은 50MB 이하로 선택해 주세요.');
      if (current.current.some((item) => item.mediaType === kind)) throw new Error('이미지와 영상은 각각 1개씩 첨부할 수 있어요. 기존 첨부를 먼저 취소해 주세요.');
      pending.current = true;
      setBusy(true);
      const uploaded = await uploadMedia(file);
      if (!mounted.current) { await discardMedia(uploaded.path); return; }
      assign([...current.current, { ...uploaded, name: file.name, preview: URL.createObjectURL(file) }]);
    } catch (err) { if (mounted.current) setError(err.message); }
    finally { pending.current = false; if (mounted.current) setBusy(false); }
  }

  async function remove(item) {
    if (pending.current || publishing.current) return;
    pending.current = true;
    setBusy(true);
    setError('');
    try {
      await discardMedia(item.path);
      assign(current.current.filter((entry) => entry.path !== item.path));
      URL.revokeObjectURL(item.preview);
    } catch (err) { if (mounted.current) setError(err.message); }
    finally { pending.current = false; if (mounted.current) setBusy(false); }
  }

  function beginPublish() {
    if (pending.current || publishing.current) return false;
    publishing.current = true;
    return true;
  }
  function finishPublish(success) {
    if (success) {
      current.current.forEach((item) => URL.revokeObjectURL(item.preview));
      assign([]);
      if (mounted.current) setError('');
    }
    publishing.current = false;
  }

  return { items, busy, error, add, remove, beginPublish, finishPublish };
}
