import React, { useEffect, useState } from "react";
import { notificationApi, getErrorMessage } from "../services/api";

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    notificationApi.list()
      .then((res) => setNotifications(res.data))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page-loading">Loading notifications...</div>;

  return (
    <div className="page">
      <h2>Notifications</h2>
      {error && <div className="alert alert-error">{error}</div>}
      {notifications.length === 0 ? (
        <div className="empty-state"><p>You have no notifications yet.</p></div>
      ) : (
        <ul className="notification-list">
          {notifications.map((n) => (
            <li key={n.id} className={n.read ? "" : "unread"}>
              <div>{n.message}</div>
              <div className="muted">{new Date(n.createdAt).toLocaleString()}</div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
