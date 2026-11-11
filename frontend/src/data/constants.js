/** 화면 필터·입력용 상수. 사용자/게시글 예시 데이터는 두지 않는다. */

export const TECH_TAGS = ['React', 'TypeScript', 'Spring', 'Java', 'Docker', 'AI'];

export const TALENT_SKILLS = ['Java', 'Spring', 'React', 'TypeScript', 'Kotlin', 'Redis', 'Docker', 'Python'];

// 회원 지역. 서버 Region enum과 같은 코드를 쓴다(프로필 편집, 인재 찾기 필터).
export const REGIONS = [
  { id: 'SEOUL', label: '서울' },
  { id: 'GYEONGGI', label: '경기' },
  { id: 'INCHEON', label: '인천' },
  { id: 'BUSAN', label: '부산' },
  { id: 'DAEGU', label: '대구' },
  { id: 'GWANGJU', label: '광주' },
  { id: 'DAEJEON', label: '대전' },
  { id: 'ULSAN', label: '울산' },
  { id: 'JEJU', label: '제주' },
  { id: 'ETC', label: '기타' }
];

export const MAX_PUBLIC_RESUMES = 3;

export const GUEST_ME = {
  name: '게스트',
  handle: '',
  initial: '?',
  av: 'av1',
  github: '',
  profileImage: '',
};
