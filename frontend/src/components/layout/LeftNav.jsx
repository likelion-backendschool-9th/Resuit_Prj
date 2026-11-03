import { useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import Icon from '../common/Icon.jsx';
import Button from '../common/Button.jsx';
import ThemeToggle from '../common/ThemeToggle.jsx';
import SettingsMenu from './SettingsMenu.jsx';
import './LeftNav.css';

// 메뉴 항목. 순서를 바꾸거나 추가하려면 여기만 고치면 된다.
const ITEMS_TOP = [
  { to: '/', icon: 'home', label: '홈', end: true },
  { to: '/jobs', icon: 'job', label: '공고' }
];
const RESUME_CHILDREN = [
  { to: '/resumes', label: '내 이력서' },
  { to: '/feedback', label: '피드백' }
];
const ITEMS_BOTTOM = [
  { to: '/threads/1', icon: 'bell', label: '알림' },
  { to: '/profile', icon: 'user', label: '프로필' },
  { to: '/company/post', icon: 'plus', label: '공고 작성', tag: '회사' },
  { to: '/talent', icon: 'talent', label: '인재 찾기', pill: '기업' }
];

function NavItem({ item }) {
  return (
    <NavLink to={item.to} end={item.end} className={({ isActive }) => `nav-item${isActive ? ' is-active' : ''}`}>
      <Icon name={item.icon} size={22} />
      {item.label}
      {item.tag && <span className="nav-item__tag">{item.tag}</span>}
      {item.pill && <span className="nav-item__pill">{item.pill}</span>}
    </NavLink>
  );
}

export default function LeftNav() {
  const { pathname } = useLocation();
  const inResume = RESUME_CHILDREN.some((c) => pathname.startsWith(c.to));
  // undefined 면 "지금 이력서 화면에 있으면 펼침"을 따른다.
  const [resumeOpen, setResumeOpen] = useState();
  const [settingsOpen, setSettingsOpen] = useState(false);
  const open = resumeOpen === undefined ? inResume : resumeOpen;

  return (
    <nav className="left-nav" aria-label="주 메뉴">
      <div className="left-nav__top">
        <Link to="/" className="left-nav__logo">Resuit</Link>
        <ThemeToggle />
      </div>

      {ITEMS_TOP.map((it) => <NavItem key={it.to} item={it} />)}

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
            <NavLink key={c.to} to={c.to} end className={({ isActive }) => `nav-sub__item${isActive ? ' is-active' : ''}`}>
              {c.label}
            </NavLink>
          ))}
        </div>
      )}

      {ITEMS_BOTTOM.map((it) => <NavItem key={it.to} item={it} />)}

      <button type="button" className="nav-item nav-item--button" aria-expanded={settingsOpen} onClick={() => setSettingsOpen((v) => !v)}>
        <Icon name="settings" size={22} />
        설정
      </button>
      {settingsOpen && <SettingsMenu onClose={() => setSettingsOpen(false)} />}

      <Button size="lg" block to="/" state={{ compose: true }} className="left-nav__post">게시하기</Button>
    </nav>
  );
}
