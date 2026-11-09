'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAppState } from '../../context/AppStateContext.jsx';
import Icon from '../common/Icon.jsx';
import Button from '../common/Button.jsx';
import ThemeToggle from '../common/ThemeToggle.jsx';
import SettingsMenu from './SettingsMenu.jsx';
import './LeftNav.css';

// 메뉴 항목. 순서를 바꾸거나 추가하려면 여기만 고치면 된다.
// roles가 있으면 그 역할(users.role)의 회원에게만 보인다. 없으면 모두에게 보인다. ADMIN은 모두 본다.
const ITEMS_TOP = [
  { to: '/', icon: 'home', label: '홈', end: true },
  { to: '/jobs', icon: 'job', label: '공고' }
];
const RESUME_CHILDREN = [
  { to: '/resumes', label: '내 이력서' },
  { to: '/feedback', label: '피드백' },
  { to: '/reviews/best', label: '베스트 리뷰' },
  { to: '/reviews/hall-of-fame', label: '명예의 전당' }
];
const ITEMS_BOTTOM = [
  { to: '/threads/1', icon: 'bell', label: '알림' },
  { to: '/offers', icon: 'msg', label: '받은 제안', roles: ['USER'] },
  { to: '/profile', icon: 'user', label: '프로필' },
  { to: '/credits', icon: 'coin', label: '크레딧' },
  { to: '/company/post', icon: 'plus', label: '공고 작성', tag: '회사', roles: ['COMPANY'] },
  { to: '/talent', icon: 'talent', label: '인재 찾기', pill: '기업', roles: ['COMPANY'] },
  { to: '/company/offers', icon: 'msg', label: '보낸 제안', roles: ['COMPANY'] }
];

// 역할별 메뉴를 보여 줄지 정한다. 로그인 전(역할을 모를 때)에는 역할별 메뉴를 숨긴다.
const visibleFor = (role) => (item) => !item.roles || role === 'ADMIN' || item.roles.includes(role);

function NavItem({ item, pathname }) {
  const active = item.end ? pathname === item.to : pathname === item.to || pathname.startsWith(`${item.to}/`);
  return (
    <Link href={item.to} aria-current={active ? 'page' : undefined} className={`nav-item${active ? ' is-active' : ''}`}>
      <Icon name={item.icon} size={22} />
      {item.label}
      {item.tag && <span className="nav-item__tag">{item.tag}</span>}
      {item.pill && <span className="nav-item__pill">{item.pill}</span>}
    </Link>
  );
}

export default function LeftNav() {
  const pathname = usePathname();
  const { requestCompose, me } = useAppState();
  const inResume = RESUME_CHILDREN.some((c) => pathname.startsWith(c.to));
  // undefined 면 "지금 이력서 화면에 있으면 펼침"을 따른다.
  const [resumeOpen, setResumeOpen] = useState();
  const [settingsOpen, setSettingsOpen] = useState(false);
  const open = resumeOpen === undefined ? inResume : resumeOpen;

  return (
    <nav className="left-nav" aria-label="주 메뉴">
      <div className="left-nav__top">
        <Link href="/" className="left-nav__logo">Resuit</Link>
        <ThemeToggle />
      </div>

      {ITEMS_TOP.map((it) => <NavItem key={it.to} item={it} pathname={pathname} />)}

      <button
        type="button"
        className={`nav-item nav-item--button${inResume ? ' is-active' : ''}`}
        aria-expanded={open}
        onClick={() => setResumeOpen(!open)}
      >
        <Icon name="file" size={22} />
        이력서
        <Icon name="chevronDown" size={16} className={`nav-item__chevron${open ? ' is-open' : ''}`} />
      </button>
      {open && (
        <div className="nav-sub">
          {RESUME_CHILDREN.map((c) => (
            <Link key={c.to} href={c.to} aria-current={pathname === c.to ? 'page' : undefined} className={`nav-sub__item${pathname === c.to ? ' is-active' : ''}`}>
              {c.label}
            </Link>
          ))}
        </div>
      )}

      {ITEMS_BOTTOM.filter(visibleFor(me.role)).map((it) => <NavItem key={it.to} item={it} pathname={pathname} />)}

      <button type="button" className="nav-item nav-item--button" aria-expanded={settingsOpen} onClick={() => setSettingsOpen((v) => !v)}>
        <Icon name="settings" size={22} />
        설정
      </button>
      {settingsOpen && <SettingsMenu onClose={() => setSettingsOpen(false)} />}

      <Button size="lg" block to="/" onClick={requestCompose} className="left-nav__post">게시하기</Button>
    </nav>
  );
}
