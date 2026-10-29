import { useTheme } from '../../context/ThemeContext.jsx';
import Icon from './Icon.jsx';
import Button, { IconButton } from './Button.jsx';

// withLabel: 아이콘 + "다크 모드" 글자 버튼 / 기본: 동그란 아이콘 버튼
export default function ThemeToggle({ withLabel = false }) {
  const { theme, toggleTheme } = useTheme();
  const dark = theme === 'dark';
  const next = dark ? '라이트 모드' : '다크 모드';

  if (withLabel) {
    return (
      <Button variant="outline" onClick={toggleTheme} aria-pressed={dark}>
        <Icon name={dark ? 'sun' : 'moon'} size={18} />
        {next}
      </Button>
    );
  }
  return (
    <IconButton label={`${next}로 전환`} onClick={toggleTheme} aria-pressed={dark}>
      <Icon name={dark ? 'sun' : 'moon'} />
    </IconButton>
  );
}
