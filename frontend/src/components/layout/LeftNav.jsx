'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAppState } from '../../context/AppStateContext.jsx';
import Icon from '../common/Icon.jsx';
import Button, { IconButton } from '../common/Button.jsx';
import ThemeToggle from '../common/ThemeToggle.jsx';
import SettingsMenu from './SettingsMenu.jsx';
import { requestFeedTop } from '../feed/feedTop.js';
import ComposeModal from '../feed/ComposeModal.jsx';
import useUnreadNotifications from '../notifications/useUnreadNotifications.js';
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
  { to: '/mentors', label: '현직자 피드백' },
  { to: '/paid-feedback', label: '내 유료 피드백' },
  { to: '/reviews/best', label: '베스트 리뷰' },
  { to: '/reviews/hall-of-fame', label: '명예의 전당' }
];
const COMPANY_CHILDREN = [
  { to: '/company/jobs', label: '내가 작성한 공고' },
  { to: '/company/applications', label: '지원 현황' },
  { to: '/company/post', label: '공고 작성' },
];
const ITEMS_BOTTOM = [
  { to: '/notifications', icon: 'bell', label: '알림' },
  { to: '/offers', icon: 'msg', label: '받은 제안', roles: ['USER'] },
  { to: '/profile', icon: 'user', label: '프로필' },
  { to: '/credits', icon: 'coin', label: '크레딧', roles: ['USER'] }
];
// 기업 회원 메뉴. 공고 관리 묶음 바로 아래에 모아 둔다. 공고 작성은 공고 관리 안에 있다.
const COMPANY_ITEMS = [
  { to: '/talent', icon: 'talent', label: '인재 찾기', pill: '기업' },
  { to: '/company/offers', icon: 'msg', label: '보낸 제안' },
  { to: '/company/pro', icon: 'coin', label: 'Pro 결제' }
];

// 역할별 메뉴를 보여 줄지 정한다. 로그인 전(역할을 모를 때)에는 역할별 메뉴를 숨긴다.
const visibleFor = (role) => (item) => !item.roles || role === 'ADMIN' || item.roles.includes(role);
// 기업 메뉴(공고 관리, 인재 찾기, 보낸 제안)는 기업 회원과 관리자만 본다.
const isCompanyMember = (role) => role === 'COMPANY' || role === 'ADMIN';
// 메뉴 항목이 지금 주소와 맞는지 본다.
const isItemActive = (item, pathname) => (item.end ? pathname === item.to : pathname === item.to || pathname.startsWith(`${item.to}/`));
// 홈에서 홈을 다시 누르면 피드를 맨 위로 올리고 새로 불러온다.
const homeClick = (item, pathname) => (item.to === '/' && pathname === '/' ? requestFeedTop : undefined);

// 모바일 하단 바에 둘 메뉴. 위 메뉴 목록에서 골라 쓰므로 메뉴를 고칠 때 한 곳만 고치면 된다.
const MOBILE_TABS = [
  ITEMS_TOP[0],
  ITEMS_TOP[1],
  { to: '/resumes', icon: 'file', label: '이력서', children: RESUME_CHILDREN },
  ITEMS_BOTTOM.find((item) => item.to === '/profile')
];

function NavItem({ item, pathname, count = 0 }) {
  const active = isItemActive(item, pathname);
  return (
    <Link href={item.to} aria-current={active ? 'page' : undefined} className={`nav-item${active ? ' is-active' : ''}`}
      onClick={homeClick(item, pathname)}>
      <Icon name={item.icon} size={22} />
      {item.label}
      {item.tag && <span className="nav-item__tag">{item.tag}</span>}
      {item.pill && <span className="nav-item__pill">{item.pill}</span>}
      {count > 0 && <span className="nav-item__count" aria-label={`안 읽은 알림 ${count}개`}>{count > 99 ? '99+' : count}</span>}
    </Link>
  );
}

export default function LeftNav() {
  const pathname = usePathname();
  const router = useRouter();
  const { me, loggedIn } = useAppState();
  // 안 읽은 알림 수. 알림 메뉴와 모바일 ☰ 버튼에 표시한다.
  const unread = useUnreadNotifications(loggedIn, pathname);
  const inResume = RESUME_CHILDREN.some((c) => pathname.startsWith(c.to));
  // 하위 메뉴는 그 주소와 그 아래 상세 화면(/mentors/아이디 등)에서 선택된 것으로 보인다.
  const isSubActive = (to) => pathname === to || pathname.startsWith(`${to}/`);
  // undefined 면 "지금 이력서 화면에 있으면 펼침"을 따른다.
  const [resumeOpen, setResumeOpen] = useState();
  const inCompany = COMPANY_CHILDREN.some((item) => pathname === item.to || pathname.startsWith(`${item.to}/`));
  const [companyExpanded, setCompanyExpanded] = useState(null);
  const companyOpen = companyExpanded?.pathname === pathname ? companyExpanded.open : inCompany;
  const [settingsOpen, setSettingsOpen] = useState(false);
  // 게시글 작성 모달. 화면 이동 없이 지금 페이지 위에 띄운다.
  const [composeOpen, setComposeOpen] = useState(false);
  const open = resumeOpen === undefined ? inResume : resumeOpen;
  // 모바일 서랍 메뉴. 연 주소를 기억해 두어 다른 화면으로 이동하면 저절로 닫힌다.
  const [drawerPath, setDrawerPath] = useState(null);
  const drawerOpen = drawerPath === pathname;
  const closeDrawer = () => setDrawerPath(null);

  // 로그인 회원만 글쓰기 모달을 연다. 비로그인이면 로그인 화면으로 보낸다.
  function openCompose() {
    closeDrawer();
    if (loggedIn) {
      setComposeOpen(true);
      return;
    }
    router.push('/login');
  }

  // 서랍 메뉴가 열려 있으면 Esc로 닫는다.
  useEffect(() => {
    if (!drawerOpen) return undefined;
    const onKey = (event) => { if (event.key === 'Escape') setDrawerPath(null); };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [drawerOpen]);

  // 하단 바의 이력서는 이력서 하위 화면 어디에서든 선택된 것으로 보인다.
  const tabActive = (item) => (item.children ? item.children.some((c) => isSubActive(c.to)) : isItemActive(item, pathname));

  return (
    <>
    {/* 모바일(800px 이하)에서만 보이는 상단 바. 데스크톱에서는 숨는다. */}
    <header className="mobile-bar">
      <IconButton label="전체 메뉴 열기" className="icon-btn--plain" aria-expanded={drawerOpen} aria-controls="main-nav"
        onClick={() => setDrawerPath(pathname)}>
        <Icon name="menu" size={22} />
        {unread > 0 && <span className="mobile-bar__dot" aria-hidden="true" />}
      </IconButton>
      <Link href="/" className="mobile-bar__logo" onClick={homeClick(ITEMS_TOP[0], pathname)}>Resuit</Link>
      <ThemeToggle />
    </header>

    {/* 데스크톱에서는 왼쪽 메뉴, 모바일에서는 ☰로 여는 서랍 메뉴다. */}
    <nav id="main-nav" className={`left-nav${drawerOpen ? ' is-open' : ''}`} aria-label="주 메뉴">
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
            <Link key={c.to} href={c.to} aria-current={isSubActive(c.to) ? 'page' : undefined} className={`nav-sub__item${isSubActive(c.to) ? ' is-active' : ''}`}>
              {c.label}
            </Link>
          ))}
        </div>
      )}

      {ITEMS_BOTTOM.filter(visibleFor(me.role)).map((it) => <NavItem key={it.to} item={it} pathname={pathname} count={it.to === '/notifications' ? unread : 0} />)}

      {isCompanyMember(me.role) && <>
        <button type="button" className={`nav-item nav-item--button nav-item--company${inCompany ? ' is-active' : ''}`}
          aria-expanded={companyOpen} aria-controls="company-nav"
          onClick={() => setCompanyExpanded({ pathname, open: !companyOpen })}>
          <Icon name="job" size={22} />공고 관리<span className="nav-item__pill">기업</span>
          <Icon name="chevronDown" size={16} className={`nav-item__chevron${companyOpen ? ' is-open' : ''}`} />
        </button>
        <div id="company-nav" className="nav-sub" hidden={!companyOpen}>
          {COMPANY_CHILDREN.map((item) => {
            const active = pathname === item.to || pathname.startsWith(`${item.to}/`);
            return <Link key={item.to} href={item.to} aria-current={active ? 'page' : undefined}
              className={`nav-sub__item${active ? ' is-active' : ''}`}>{item.label}</Link>;
          })}
        </div>
        {COMPANY_ITEMS.map((it) => <NavItem key={it.to} item={it} pathname={pathname} />)}
      </>}

      <button type="button" className="nav-item nav-item--button" aria-expanded={settingsOpen} onClick={() => setSettingsOpen((v) => !v)}>
        <Icon name="settings" size={22} />
        설정
      </button>
      {settingsOpen && <SettingsMenu onClose={() => setSettingsOpen(false)} />}

      <Button
        size="lg"
        block
        onClick={openCompose}
        aria-haspopup={loggedIn ? 'dialog' : undefined}
        className="left-nav__post"
      >
        {loggedIn ? '게시글 작성' : '로그인하기'}
      </Button>
    </nav>
    {drawerOpen && <div className="left-nav__scrim" aria-hidden="true" onClick={closeDrawer} />}

    {/* 모바일(800px 이하)에서만 보이는 하단 바. 자주 쓰는 메뉴와 게시글 작성 버튼을 둔다. */}
    <nav className="mobile-tabs" aria-label="빠른 메뉴">
      {MOBILE_TABS.slice(0, 2).map((item) => <MobileTab key={item.to} item={item} active={tabActive(item)} onClick={homeClick(item, pathname)} />)}
      <button
        type="button"
        className="mobile-tabs__item mobile-tabs__post"
        aria-haspopup={loggedIn ? 'dialog' : undefined}
        aria-label={loggedIn ? '게시글 작성' : '로그인하기'}
        onClick={openCompose}
      >
        <span className="mobile-tabs__post-icon"><Icon name="plus" size={22} /></span>
        {loggedIn ? '작성' : '로그인'}
      </button>
      {/* 기업 회원은 이력서 대신 인재 찾기를 둔다. */}
      {(isCompanyMember(me.role) && me.role !== 'ADMIN' ? [COMPANY_ITEMS[0], MOBILE_TABS[3]] : MOBILE_TABS.slice(2))
        .map((item) => <MobileTab key={item.to} item={item} active={tabActive(item)} />)}
    </nav>
    {composeOpen && <ComposeModal onClose={() => setComposeOpen(false)} />}
    </>
  );
}

function MobileTab({ item, active, onClick }) {
  return (
    <Link href={item.to} aria-current={active ? 'page' : undefined} className={`mobile-tabs__item${active ? ' is-active' : ''}`} onClick={onClick}>
      <Icon name={item.icon} size={22} />
      {item.label}
    </Link>
  );
}
