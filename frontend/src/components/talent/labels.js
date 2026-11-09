// 인재 찾기에서 서버 값을 화면 문구로 바꾼다.

// 분야 탭. 이력서 분야 값과 같은 코드를 쓴다.
export const TALENT_FIELDS = [
  { id: 'BACKEND', label: '백엔드' },
  { id: 'FRONTEND', label: '프론트엔드' },
  { id: 'FULLSTACK', label: '풀스택' }
];
export const FIELD_LABEL = Object.fromEntries(TALENT_FIELDS.map((f) => [f.id, f.label]));

// 경력 구간. 서버 필터(career)와 같은 코드를 쓴다.
export const CAREERS = [
  { id: 'ENTRY', label: '신입' },
  { id: 'ONE_TO_THREE', label: '1~3년' },
  { id: 'THREE_PLUS', label: '3년 이상' }
];
export const CAREER_LABEL = Object.fromEntries(CAREERS.map((c) => [c.id, c.label]));

// 지역. 서버 Region enum과 같은 코드를 쓴다.
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
export const REGION_LABEL = Object.fromEntries(REGIONS.map((r) => [r.id, r.label]));
