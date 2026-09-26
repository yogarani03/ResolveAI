import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { dashboardApi, getErrorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import { StatCard } from "./UserDashboard";

export default function StaffDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    dashboardApi.staff().then((res) => setData(res.data)).catch((err) => setError(getErrorMessage(err)));
  }, []);

  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  if (!data) return <div className="page-loading">Loading dashboard...</div>;

  return (
    <div className="page">
      <h2>Staff Dashboard</h2>
      <div className="stat-grid">
        <StatCard label="Assigned to Me" value={data.totalComplaints} />
        <StatCard label="In Progress" value={data.inProgressComplaints} />
        <StatCard label="Escalated" value={data.escalatedComplaints} />
        <StatCard label="Resolved" value={data.resolvedComplaints} />
      </div>

      <div className="card">
        <h3>Escalated Complaints</h3>
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
        <h3>Recently Assigned</h3>
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
