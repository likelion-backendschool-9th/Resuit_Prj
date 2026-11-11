import Avatar from '../common/Avatar.jsx';
import Icon from '../common/Icon.jsx';
import { avatarColor, timeText } from '../feedback/labels.js';
import './NotificationItem.css';

// 알림 한 줄. 누르면 할 일(읽음 처리, 이동)은 페이지에서 onOpen으로 받는다.
export default function NotificationItem({ notification, onOpen }) {
  const { actor } = notification;
  const name = actor ? actor.nickname || actor.username : '';
  return (
    <button type="button" className={`notification-item${notification.read ? '' : ' is-unread'}`} onClick={() => onOpen(notification)}>
      {actor
        ? <Avatar initial={name.slice(0, 1)} color={avatarColor(actor.username)} src={actor.profileImageUrl || undefined} size={40} />
        : <span className="notification-item__icon"><Icon name="bell" size={20} /></span>}
      <span className="notification-item__body">
        <span className="notification-item__message">{notification.message}</span>
        <span className="xs muted">{timeText(notification.createdAt)}</span>
      </span>
      {!notification.read && <span className="notification-item__dot" aria-label="안 읽음" />}
    </button>
  );
}
