import React, { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { complaintApi, getErrorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import { useAuth } from "../context/AuthContext";

export default function ComplaintList() {
  const { user } = useAuth();
  const [complaints, setComplaints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");

  useEffect(() => {
    complaintApi.list()
      .then((res) => setComplaints(res.data))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    return complaints.filter((c) => {
      const matchesSearch = c.title.toLowerCase().includes(search.toLowerCase());
      const matchesStatus = statusFilter === "ALL" || c.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [complaints, search, statusFilter]);

  if (loading) return <div className="page-loading">Loading complaints...</div>;

  return (
    <div className="page">
      <h2>{user.role === "USER" ? "My Complaints" : user.role === "STAFF" ? "Assigned Complaints" : "All Complaints"}</h2>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="filters">
        <input placeholder="Search by title..." value={search} onChange={(e) => setSearch(e.target.value)} />
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="ALL">All statuses</option>
          {["OPEN", "ASSIGNED", "IN_PROGRESS", "ESCALATED", "RESOLVED", "CLOSED", "REOPENED"].map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="empty-state">
          <p>No complaints found.</p>
          {user.role === "USER" && <Link className="btn btn-primary" to="/complaints/new">Create your first complaint</Link>}
        </div>
      ) : (
        <table className="table">
          <thead>
            <tr>
              <th>ID</th><th>Title</th><th>Category</th><th>Priority</th><th>Status</th><th>Created</th><th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((c) => (
              <tr key={c.id}>
                <td>#{c.id}</td>
                <td>{c.title}</td>
                <td>{c.categoryName || "—"}</td>
                <td>{c.priority}</td>
                <td><StatusBadge status={c.status} /></td>
                <td>{new Date(c.createdAt).toLocaleDateString()}</td>
                <td><Link className="btn btn-link" to={`/complaints/${c.id}`}>View</Link></td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
