// 유료 피드백 상태를 화면 문구와 배지 색으로 바꾼다.
export const STATUS = {
  REQUESTED: { label: '답변 대기', tone: 'general' },
  IN_PROGRESS: { label: '답변 중', tone: 'question' },
  COMPLETED: { label: '완료', tone: 'accepted' },
  CANCELED: { label: '취소', tone: 'special' }
};
export const statusOf = (value) => STATUS[value] || { label: value, tone: 'general' };

export const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');

// 현직자의 직무와 경력. 경력은 프로필 경력으로 계산한 연수이고, 프로필 경력이 없으면(null) 표시하지 않는다.
export function mentorRole(mentor) {
  if (mentor.careerYears == null) return mentor.jobTitle;
  return `${mentor.jobTitle} · 경력 ${mentor.careerYears < 1 ? '1년 미만' : `${mentor.careerYears}년`}`;
}
