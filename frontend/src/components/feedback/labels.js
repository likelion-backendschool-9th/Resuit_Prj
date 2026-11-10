// 피드백 게시판에서 서버 값을 화면 문구로 바꾼다
export const FIELD_LABEL = { FRONTEND: '프론트엔드', BACKEND: '백엔드', FULLSTACK: '풀스택' };

// 3.2 피드백 요청에서 고르는 이력서 항목
export const SECTION_LABEL = {
  BASIC: '기본 정보', SKILLS: '기술 스택', PROJECTS: '프로젝트', AWARDS: '수상', INTRODUCTION: '자기소개', ANSWERS: '문항 답변'
};

const AVATAR_COLORS = ['av1', 'av2', 'av3', 'av4', 'av5', 'av6'];

// 같은 사람은 항상 같은 아바타 색이 나오게 username으로 고른다
export function avatarColor(username = '') {
  let sum = 0;
  for (const ch of username) sum += ch.charCodeAt(0);
  return AVATAR_COLORS[sum % AVATAR_COLORS.length];
}

export function timeText(value) {
  const minutes = Math.floor((Date.now() - new Date(value).getTime()) / 60000);
  if (!(minutes >= 1)) return '방금';
  if (minutes < 60) return `${minutes}분 전`;
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}시간 전`;
  return `${Math.floor(minutes / (60 * 24))}일 전`;
}
