// 홈 피드를 맨 위로 올리고 새로 불러오라는 요청.
// 왼쪽 메뉴(홈을 다시 누를 때)와 홈 피드가 서로를 모르도록 브라우저 이벤트로 주고받는다.
const EVENT = 'resuit:feed-top';

export function requestFeedTop() {
  window.dispatchEvent(new Event(EVENT));
}

// 요청을 받을 함수를 등록하고, 등록을 푸는 함수를 돌려준다.
export function onFeedTopRequest(handler) {
  window.addEventListener(EVENT, handler);
  return () => window.removeEventListener(EVENT, handler);
}
