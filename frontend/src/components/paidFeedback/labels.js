// 유료 피드백 상태를 화면 문구와 배지 색으로 바꾼다.
export const STATUS = {
  REQUESTED: { label: '답변 대기', tone: 'general' },
  IN_PROGRESS: { label: '답변 중', tone: 'question' },
  COMPLETED: { label: '완료', tone: 'accepted' },
  CANCELED: { label: '취소', tone: 'special' }
};
export const statusOf = (value) => STATUS[value] || { label: value, tone: 'general' };

export const dateText = (value) => (value ? new Date(value).toLocaleDateString('ko-KR') : '');
