import { useEffect, useState } from 'react';
import { useTheme } from '../../context/ThemeContext.jsx';
import Icon from './Icon.jsx';
import Button, { IconButton } from './Button.jsx';

// withLabel: 아이콘 + "다크 모드" 글자 버튼 / 기본: 동그란 아이콘 버튼
export default function ThemeToggle({ withLabel = false }) {
  const { theme, toggleTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  useEffect(() => { setMounted(true); }, []);
  // 서버·첫 hydrate는 layout 인라인 스크립트와 어긋날 수 있어, 마운트 후에만 테마 라벨을 확정한다.
  const dark = mounted && theme === 'dark';
  const next = dark ? '라이트 모드' : '다크 모드';

  if (withLabel) {
    return (
      <Button variant="outline" onClick={toggleTheme} aria-pressed={dark}>
        <Icon name={dark ? 'sun' : 'moon'} size={18} />
        {mounted ? next : '테마'}
      </Button>
    );
  }
  return (
    <IconButton label={mounted ? `${next}로 전환` : '테마 전환'} onClick={toggleTheme} aria-pressed={dark}>
      <Icon name={dark ? 'sun' : 'moon'} />
    </IconButton>
  );
}
