import { useAppState } from '../../context/AppStateContext.jsx';
import Button from './Button.jsx';

export default function FollowButton({ handle, className }) {
  const { following, toggleFollow } = useAppState();
  const on = !!following[handle];
  return (
    <Button variant={on ? 'outline' : 'dark'} size="sm" aria-pressed={on} onClick={() => toggleFollow(handle)} className={className}>
      {on ? '팔로잉' : '팔로우'}
    </Button>
  );
}
