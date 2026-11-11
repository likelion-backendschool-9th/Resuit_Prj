'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { MainColumn, RightAside } from '@/components/layout/AppLayout.jsx';
import Button from '@/components/common/Button.jsx';
import NotificationItem from '@/components/notifications/NotificationItem.jsx';
import { announceNotificationsChanged } from '@/components/notifications/notificationEvents.js';
import { getNotifications, readAllNotifications, readNotification } from '@/services/notifications.js';

const PAGE_SIZE = 20;

// 내 알림 목록. 누르면 읽음으로 바꾸고 그 화면으로 이동한다.
export default function NotificationsPage() {
  const router = useRouter();
  // 목록 응답(Page)과 지금까지 불러온 알림
  const [result, setResult] = useState(null);
  const [items, setItems] = useState([]);
  const [error, setError] = useState(null);
  const [loadingMore, setLoadingMore] = useState(false);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let active = true;
    getNotifications({ page: 0, size: PAGE_SIZE })
      .then((data) => { if (active) { setResult(data); setItems(data.content); } })
      .catch((err) => { if (active) setError(err); });
    return () => { active = false; };
  }, []);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const data = await getNotifications({ page: result.number + 1, size: PAGE_SIZE });
      setResult(data);
      setItems((list) => [...list, ...data.content]);
    } catch (err) {
      setError(err);
    } finally {
      setLoadingMore(false);
    }
  }

  // 알림을 누르면 읽음으로 바꾸고 이동한다. 읽음 처리에 실패해도 이동은 한다.
  async function open(notification) {
    if (!notification.read) {
      setItems((list) => list.map((item) => (item.id === notification.id ? { ...item, read: true } : item)));
      await readNotification(notification.id).catch(() => {});
      announceNotificationsChanged();
    }
    router.push(notification.link);
  }

  async function readAll() {
    setBusy(true);
    try {
      await readAllNotifications();
      setItems((list) => list.map((item) => ({ ...item, read: true })));
      announceNotificationsChanged();
    } catch (err) {
      setError(err);
    } finally {
      setBusy(false);
    }
  }

  const hasUnread = items.some((item) => !item.read);

  return <>
    <MainColumn>
      <div className="page-title">
        <h1>알림</h1>
        {result && items.length > 0 && (
          <Button variant="outline" size="sm" onClick={readAll} disabled={busy || !hasUnread}>모두 읽음</Button>
        )}
      </div>
      <p className="page-desc">내 글의 댓글, 피드백, 채용 제안처럼 다른 회원이 나에게 한 일을 알려 드려요.</p>
      {error?.status === 401 ? (
        <div className="empty">
          <p>로그인한 뒤 볼 수 있어요.</p>
          <Button to="/login" variant="primary" size="sm">로그인하기</Button>
        </div>
      ) : error ? (
        <div className="empty" role="alert">{error.message}</div>
      ) : result === null ? (
        <div className="empty">불러오는 중…</div>
      ) : items.length === 0 ? (
        <div className="empty">아직 받은 알림이 없어요.</div>
      ) : (
        <>
          {items.map((item) => <NotificationItem key={item.id} notification={item} onOpen={open} />)}
          {!result.last && (
            <div style={{ display: 'flex', justifyContent: 'center', padding: 16 }}>
              <Button variant="outline" size="sm" onClick={loadMore} disabled={loadingMore}>{loadingMore ? '불러오는 중…' : '더 보기'}</Button>
            </div>
          )}
        </>
      )}
    </MainColumn>
    <RightAside label="알림 안내" />
  </>;
}
