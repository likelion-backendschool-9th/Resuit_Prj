'use client';

import { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAppState } from '../../context/AppStateContext.jsx';
import Icon from '../common/Icon.jsx';
import Button from '../common/Button.jsx';
import ThemeToggle from '../common/ThemeToggle.jsx';
import SettingsMenu from './SettingsMenu.jsx';
import ComposeModal from '../feed/ComposeModal.jsx';
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
  { to: '/threads/1', icon: 'bell', label: '알림' },
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
  const { me } = useAppState();
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
            <Link key={c.to} href={c.to} aria-current={isSubActive(c.to) ? 'page' : undefined} className={`nav-sub__item${isSubActive(c.to) ? ' is-active' : ''}`}>
              {c.label}
            </Link>
          ))}
        </div>
      )}

      {ITEMS_BOTTOM.filter(visibleFor(me.role)).map((it) => <NavItem key={it.to} item={it} pathname={pathname} />)}

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

      <Button size="lg" block onClick={() => setComposeOpen(true)} aria-haspopup="dialog" className="left-nav__post">게시글 작성</Button>
      {composeOpen && <ComposeModal onClose={() => setComposeOpen(false)} />}
    </nav>
  );
}
