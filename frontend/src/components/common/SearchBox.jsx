'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Icon from './Icon.jsx';
import './SearchBox.css';

export default function SearchBox({ placeholder = '#태그로 게시글 검색' }) {
  const router = useRouter();
  const [value, setValue] = useState('');

  function submit(event) {
    event.preventDefault();
    const raw = value.trim();
    if (!raw) return;
    if (!raw.startsWith('#')) {
      // 1차는 해시태그 검색만 지원한다.
      return;
    }
    const tag = raw.replace(/^#+/, '').trim();
    if (!tag) return;
    router.push(`/search?hashtag=${encodeURIComponent(tag)}`);
  }

  return (
    <form className="search-box" onSubmit={submit} role="search">
      <Icon name="search" size={18} />
      <label className="sr-only" htmlFor="aside-search">검색</label>
      <input
        id="aside-search"
        type="search"
        placeholder={placeholder}
        value={value}
        onChange={(event) => setValue(event.target.value)}
      />
    </form>
  );
}
