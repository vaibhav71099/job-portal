export default function NotificationsSection({ user, notifications, handleMarkNotificationRead, markAllNotificationsRead }) {
  if (!user) return null;
  return (
    <section className="panel">
      <div className="panel-header">
        <h2>Notifications</h2>
        <button onClick={markAllNotificationsRead}>Mark All Read</button>
      </div>
      <div className="grid">
        {notifications.length === 0 && <article className="card empty-card"><h3>No notifications</h3></article>}
        {notifications.map((n) => (
          <article className="card" key={n.id}>
            <p><strong>{n.type}</strong></p>
            <p>{n.message}</p>
            <p className="muted">{n.createdAt}</p>
            {!n.read && <button onClick={() => handleMarkNotificationRead(n.id)}>Mark as read</button>}
          </article>
        ))}
      </div>
    </section>
  );
}
