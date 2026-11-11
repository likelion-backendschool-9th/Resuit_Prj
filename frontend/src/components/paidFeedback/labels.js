// 유료 피드백 상태를 화면 문구와 배지 색으로 바꾼다.
export const STATUS = {
  REQUESTED: { label: '답변 대기', tone: 'general' },
  IN_PROGRESS: { label: '답변 중', tone: 'question' },
  COMPLETED: { label: '완료', tone: 'accepted' },
  CANCELED: { label: '취소', tone: 'special' }
};
export const statusOf = (value) => STATUS[value] || { label: value, tone: 'general' };

export const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');

// 현직자 직무. 이력서 분야와 같은 값을 쓴다.
export const JOB_OPTIONS = [
  { id: 'BACKEND', label: '백엔드' },
  { id: 'FRONTEND', label: '프론트엔드' },
  { id: 'FULLSTACK', label: '풀스택' }
];
// 직무 코드를 화면 문구로 바꾼다. 직접 입력하던 때의 값은 그대로 보여 준다.
export const jobLabel = (value) => JOB_OPTIONS.find((o) => o.id === value)?.label || value;

// 현직자의 직무와 경력. 경력은 프로필 경력으로 계산한 연수이고, 프로필 경력이 없으면(null) 표시하지 않는다.
export function mentorRole(mentor) {
  const job = jobLabel(mentor.jobTitle);
  if (mentor.careerYears == null) return job;
  return `${job} · 경력 ${mentor.careerYears < 1 ? '1년 미만' : `${mentor.careerYears}년`}`;
}
