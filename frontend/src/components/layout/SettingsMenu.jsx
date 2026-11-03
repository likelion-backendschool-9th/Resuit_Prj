import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import './SettingsMenu.css';

// 왼쪽 메뉴 "설정"을 누르면 펼쳐지는 계정 메뉴
export default function SettingsMenu({ onClose }) {
  const { me, logout } = useAppState();
  const router = useRouter();

  const signOut = () => {
    logout();
    onClose();
    router.push('/onboarding');
  };

  return (
    <div className="settings-menu">
      <div className="settings-menu__account">
        <Avatar initial={me.initial} color={me.av} size={32} />
        <div className="grow">
          <div className="bold" style={{ fontSize: 14 }}>{me.name}</div>
          <div className="xs muted">@{me.handle} · GitHub 로그인</div>
        </div>
      </div>
      <hr className="settings-menu__divider" />
      <Link href="/profile" className="settings-menu__item" onClick={onClose}>프로필 편집</Link>
      <Link href="/onboarding" className="settings-menu__item" onClick={onClose}>연동 관리 (GitHub · 블로그)</Link>
      <button type="button" className="settings-menu__item" onClick={signOut}>다른 계정으로 로그인</button>
      <button type="button" className="settings-menu__item settings-menu__item--danger" onClick={signOut}>로그아웃</button>
    </div>
  );
}
