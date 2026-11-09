import Link from 'next/link';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAppState } from '../../context/AppStateContext.jsx';
import Avatar from '../common/Avatar.jsx';
import './SettingsMenu.css';

// 왼쪽 메뉴 "설정"을 누르면 펼쳐지는 계정 메뉴
export default function SettingsMenu({ onClose }) {
  const { me, logout, loggedIn } = useAppState();
  const router = useRouter();

  const [error, setError] = useState('');
  const signOut = async () => {
    try { await logout(); }
    catch (err) { setError(err.message); return; }
    onClose();
    router.push('/login');
  };

  return (
    <div className="settings-menu">
      <div className="settings-menu__account">
        <Avatar initial={me.initial} color={me.av} size={32} />
        <div className="grow">
          <div className="bold" style={{ fontSize: 14 }}>{me.name}</div>
          <div className="xs muted">{loggedIn ? `@${me.handle} · 로그인됨` : '로그인하지 않았어요'}</div>
        </div>
      </div>
      {error && <p role="alert">{error}</p>}
      <hr className="settings-menu__divider" />
      <Link href="/setting/profile" className="settings-menu__item" onClick={onClose}>프로필 편집</Link>
      <Link href="/setting/integrations" className="settings-menu__item" onClick={onClose}>연동 관리 (GitHub · 블로그)</Link>
      {!loggedIn && <Link href="/login" className="settings-menu__item" onClick={onClose}>로그인</Link>}
      {loggedIn && <button type="button" className="settings-menu__item settings-menu__item--danger" onClick={signOut}>로그아웃</button>}
    </div>
  );
}
