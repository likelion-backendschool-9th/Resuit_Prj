'use client';

import { useEffect, useState } from 'react';
import { getUnreadNotificationCount } from '../../services/notifications.js';
import { onNotificationsChanged } from './notificationEvents.js';

// 안 읽은 알림 수. 화면을 옮길 때, 창으로 돌아올 때, 알림을 읽었을 때 다시 불러온다.
// 로그인하지 않았으면(enabled=false) 부르지 않고 0이다.
export default function useUnreadNotifications(enabled, pathname) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    if (!enabled) return undefined;
    let active = true;
    const load = () => getUnreadNotificationCount()
      .then((data) => { if (active) setCount(data.unread); })
      .catch(() => {});
    load();
    window.addEventListener('focus', load);
    const off = onNotificationsChanged(load);
    return () => {
      active = false;
      window.removeEventListener('focus', load);
      off();
    };
  }, [enabled, pathname]);

  return enabled ? count : 0;
}
