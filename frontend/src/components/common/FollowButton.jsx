'use client';

import { useEffect, useState } from 'react';
import { useAppState } from '../../context/AppStateContext.jsx';
import { profileApi } from '../../services/profileApi.js';
import Button from './Button.jsx';

// offVariant: 아직 팔로우하지 않았을 때의 버튼 모양 (공고 상세는 outline)
export default function FollowButton({
  handle,
  className,
  offVariant = 'dark',
  initialFollowing = false,
  onChanged,
}) {
  const { following, setFollowState, loggedIn, me } = useAppState();
  const known = Object.prototype.hasOwnProperty.call(following, handle);
  const [on, setOn] = useState(known ? !!following[handle] : !!initialFollowing);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (Object.prototype.hasOwnProperty.call(following, handle)) {
      setOn(!!following[handle]);
    } else {
      setOn(!!initialFollowing);
    }
  }, [following, handle, initialFollowing]);

  if (!handle || handle === me.handle) return null;

  const toggle = async () => {
    if (busy) return;
    if (!loggedIn) {
      const next = !on;
      setFollowState(handle, next);
      setOn(next);
      onChanged?.({ isFollowing: next });
      return;
    }
    setBusy(true);
    try {
      const result = on
        ? await profileApi.unfollow(handle, me.handle)
        : await profileApi.follow(handle, me.handle);
      const next = !!result.isFollowing;
      setOn(next);
      setFollowState(handle, next);
      onChanged?.(result);
    } catch {
      /* keep previous */
    } finally {
      setBusy(false);
    }
  };

  return (
    <Button
      variant={on ? 'outline' : offVariant}
      size="sm"
      aria-pressed={on}
      disabled={busy}
      onClick={toggle}
      className={className}
    >
      {on ? '팔로잉' : '팔로우'}
    </Button>
  );
}
