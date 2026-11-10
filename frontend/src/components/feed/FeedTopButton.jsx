'use client';

import { useEffect, useState } from 'react';
import { IconButton } from '../common/Button.jsx';
import Icon from '../common/Icon.jsx';
import './FeedTopButton.css';

// 이만큼 내려가면 버튼을 보여 준다(px).
const SHOW_AFTER = 400;

// 피드 오른쪽 아래에 떠 있는 "맨 위로 + 새 글 불러오기" 버튼. 누를 때 할 일은 onClick으로 받는다.
export default function FeedTopButton({ onClick }) {
  const [visible, setVisible] = useState(false);

  // 스크롤 위치는 브라우저에만 있어서 useEffect에서 읽는다.
  useEffect(() => {
    const update = () => setVisible(window.scrollY > SHOW_AFTER);
    update();
    window.addEventListener('scroll', update, { passive: true });
    return () => window.removeEventListener('scroll', update);
  }, []);

  return (
    <div className="feed-top">
      <IconButton label="맨 위로 이동하고 새 글 불러오기" title="맨 위로 · 새 글 불러오기"
        className={`feed-top__btn${visible ? ' is-on' : ''}`} tabIndex={visible ? 0 : -1} onClick={onClick}>
        <Icon name="arrowUp" size={22} />
      </IconButton>
    </div>
  );
}
