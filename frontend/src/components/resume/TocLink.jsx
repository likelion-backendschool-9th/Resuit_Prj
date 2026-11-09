'use client';

// 이력서 편집 화면의 왼쪽 목차: 누른 섹션을 표시하고 그 위치로 스크롤한다.
export default function TocLink({ target, active, onPick, children }) {
  const go = () => {
    onPick(target);
    document.getElementById(target)?.scrollIntoView({ behavior: 'smooth' });
  };
  return (
    <button type="button" className={`resume-edit__toc-link${active === target ? ' is-active' : ''}`} aria-current={active === target ? 'true' : undefined} onClick={go}>
      {children}
    </button>
  );
}
