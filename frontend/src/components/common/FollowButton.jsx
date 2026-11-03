import { useAppState } from '../../context/AppStateContext.jsx';
import Button from './Button.jsx';

// offVariant: 아직 팔로우하지 않았을 때의 버튼 모양 (공고 상세는 outline)
export default function FollowButton({ handle, className, offVariant = 'dark' }) {
  const { following, toggleFollow } = useAppState();
  const on = !!following[handle];
  return (
    <Button variant={on ? 'outline' : offVariant} size="sm" aria-pressed={on} onClick={() => toggleFollow(handle)} className={className}>
      {on ? '팔로잉' : '팔로우'}
    </Button>
  );
}
