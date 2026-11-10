// 알림을 읽어서 안 읽은 수가 바뀌었다는 신호.
// 알림 화면과 왼쪽 메뉴의 개수 배지가 서로를 모르도록 브라우저 이벤트로 주고받는다.
const EVENT = 'resuit:notifications-changed';

export function announceNotificationsChanged() {
  window.dispatchEvent(new Event(EVENT));
}

// 신호를 받을 함수를 등록하고, 등록을 푸는 함수를 돌려준다.
export function onNotificationsChanged(handler) {
  window.addEventListener(EVENT, handler);
  return () => window.removeEventListener(EVENT, handler);
}
