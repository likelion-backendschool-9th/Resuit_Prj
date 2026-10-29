import Icon from './Icon.jsx';
import './SearchBox.css';

export default function SearchBox({ placeholder = '사람, 질문, 공고 검색' }) {
  return (
    <label className="search-box">
      <Icon name="search" size={18} />
      <span className="sr-only">검색</span>
      <input type="search" placeholder={placeholder} />
    </label>
  );
}
