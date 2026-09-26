import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { dashboardApi, getErrorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import { StatCard } from "./UserDashboard";

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    dashboardApi.admin().then((res) => setData(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  if (!data) return <div className="page-loading">Loading dashboard...</div>;

  return (
    <div className="page">
      <h2>Admin Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Total Complaints" value={data.totalComplaints} />
        <StatCard label="Open" value={data.openComplaints} />
        <StatCard label="Assigned" value={data.assignedComplaints} />
        <StatCard label="In Progress" value={data.inProgressComplaints} />
        <StatCard label="Escalated" value={data.escalatedComplaints} />
        <StatCard label="Resolved" value={data.resolvedComplaints} />
        <StatCard label="Closed" value={data.closedComplaints} />
        <StatCard label="Avg. Resolution Time"
                   value={data.averageResolutionTimeHours != null ? `${data.averageResolutionTimeHours.toFixed(1)}h` : "—"} />
        <StatCard label="Avg. Feedback Rating"
                   value={data.averageFeedbackRating != null ? `${data.averageFeedbackRating.toFixed(1)} / 5` : "—"} />
        <StatCard label="Feedback Count" value={data.totalFeedbackCount} />
      </div>

      <div className="grid-2">
        <div className="card">
          <h3>Complaints by Category</h3>
          {Object.keys(data.complaintsByCategory).length === 0 ? <p className="muted">No data yet.</p> : (
            <ul>
              {Object.entries(data.complaintsByCategory).map(([k, v]) => <li key={k}>{k}: {v}</li>)}
            </ul>
          )}
        </div>
        <div className="card">
          <h3>Complaints by Department</h3>
          {Object.keys(data.complaintsByDepartment).length === 0 ? <p className="muted">No data yet.</p> : (
            <ul>
              {Object.entries(data.complaintsByDepartment).map(([k, v]) => <li key={k}>{k}: {v}</li>)}
            </ul>
          )}
        </div>
      </div>

      <div className="card">
        <h3>Recent Escalations</h3>
        {data.recentEscalations.length === 0 ? <p className="muted">No escalated complaints right now.</p> : (
          <table className="table">
            <thead><tr><th>ID</th><th>Title</th><th>Status</th></tr></thead>
            <tbody>
              {data.recentEscalations.map((c) => (
                <tr key={c.id}>
                  <td><Link to={`/complaints/${c.id}`}>#{c.id}</Link></td>
                  <td>{c.title}</td>
                  <td><StatusBadge status={c.status} /></td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="card">
        <h3>Recent Complaints</h3>
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
      </div>
    </div>
  );
}
