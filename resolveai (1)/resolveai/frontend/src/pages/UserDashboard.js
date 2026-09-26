import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { dashboardApi, getErrorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";

export default function UserDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    dashboardApi.user().then((res) => setData(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  if (!data) return <div className="page-loading">Loading dashboard...</div>;

  return (
    <div className="page">
      <h2>My Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Total Complaints" value={data.totalComplaints} />
        <StatCard label="Open" value={data.openComplaints} />
        <StatCard label="In Progress" value={data.inProgressComplaints} />
        <StatCard label="Escalated" value={data.escalatedComplaints} />
        <StatCard label="Resolved" value={data.resolvedComplaints} />
        <StatCard label="Closed" value={data.closedComplaints} />
      </div>

      <div className="card">
        <h3>Recent Complaints</h3>
        {data.recentComplaints.length === 0 ? (
          <div className="empty-state">
            <p>You haven't submitted any complaints yet.</p>
            <Link className="btn btn-primary" to="/complaints/new">Create your first complaint</Link>
          </div>
        ) : (
          <table className="table">
            <thead><tr><th>ID</th><th>Title</th><th>Status</th><th>Created</th></tr></thead>
            <tbody>
              {data.recentComplaints.map((c) => (
                <tr key={c.id}>
                  <td><Link to={`/complaints/${c.id}`}>#{c.id}</Link></td>
                  <td>{c.title}</td>
                  <td><StatusBadge status={c.status} /></td>
                  <td>{new Date(c.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export function StatCard({ label, value }) {
  return (
    <div className="stat-card">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}
