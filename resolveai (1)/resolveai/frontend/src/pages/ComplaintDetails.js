import React, { useEffect, useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import { complaintApi, referenceApi, getErrorMessage } from "../services/api";
import { useAuth } from "../context/AuthContext";
import StatusBadge from "../components/StatusBadge";

const NEXT_STATUS_OPTIONS = {
  OPEN: ["ASSIGNED", "IN_PROGRESS", "ESCALATED"],
  ASSIGNED: ["IN_PROGRESS", "ESCALATED"],
  IN_PROGRESS: ["RESOLVED", "ESCALATED"],
  ESCALATED: ["IN_PROGRESS", "RESOLVED"],
  RESOLVED: ["CLOSED", "REOPENED"],
  CLOSED: ["REOPENED"],
  REOPENED: ["ASSIGNED", "IN_PROGRESS"],
};

export default function ComplaintDetails() {
  const { id } = useParams();
  const { user } = useAuth();

  const [complaint, setComplaint] = useState(null);
  const [history, setHistory] = useState([]);
  const [related, setRelated] = useState([]);
  const [staffList, setStaffList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionMessage, setActionMessage] = useState("");

  // form state for staff/admin actions
  const [selectedStaffId, setSelectedStaffId] = useState("");
  const [nextStatus, setNextStatus] = useState("");
  const [statusNote, setStatusNote] = useState("");
  const [resolutionNotes, setResolutionNotes] = useState("");
  const [suggestion, setSuggestion] = useState("");
  const [suggestionLoading, setSuggestionLoading] = useState(false);

  // feedback form state
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  const load = useCallback(() => {
    setLoading(true);
    setError("");
    Promise.all([
      complaintApi.getById(id),
      complaintApi.history(id).catch(() => ({ data: [] })),
      complaintApi.related(id).catch(() => ({ data: [] })),
    ])
      .then(([c, h, r]) => {
        setComplaint(c.data);
        setHistory(h.data);
        setRelated(r.data);
      })
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    if (user.role === "STAFF" || user.role === "ADMIN") {
      referenceApi.staff().then((res) => setStaffList(res.data)).catch(() => {});
    }
  }, [user.role]);

  async function handleAssign(e) {
    e.preventDefault();
    setActionMessage("");
    try {
      await complaintApi.assign(id, { staffId: Number(selectedStaffId) });
      setActionMessage("Staff assigned successfully.");
      load();
    } catch (err) {
      setActionMessage(getErrorMessage(err));
    }
  }

  async function handleStatusUpdate(e) {
    e.preventDefault();
    setActionMessage("");
    try {
      await complaintApi.updateStatus(id, { status: nextStatus, note: statusNote });
      setActionMessage("Status updated successfully.");
      setStatusNote("");
      load();
    } catch (err) {
      setActionMessage(getErrorMessage(err));
    }
  }

  async function handleResolve(e) {
    e.preventDefault();
    setActionMessage("");
    try {
      await complaintApi.resolve(id, { resolutionNotes });
      setActionMessage("Complaint marked as resolved.");
      load();
    } catch (err) {
      setActionMessage(getErrorMessage(err));
    }
  }

  async function handleGetSuggestion() {
    setSuggestionLoading(true);
    try {
      const res = await complaintApi.resolutionSuggestion(id);
      setSuggestion(res.data.suggestion);
    } catch (err) {
      setSuggestion("Could not fetch a suggestion right now.");
    } finally {
      setSuggestionLoading(false);
    }
  }

  async function handleFeedback(e) {
    e.preventDefault();
    setActionMessage("");
    try {
      await complaintApi.submitFeedback(id, { rating, comment });
      setActionMessage("Thank you - your feedback has been submitted!");
      load();
    } catch (err) {
      setActionMessage(getErrorMessage(err));
    }
  }

  if (loading) return <div className="page-loading">Loading complaint...</div>;
  if (error) return <div className="page"><div className="alert alert-error">{error}</div></div>;
  if (!complaint) return null;

  const canManage = user.role === "STAFF" || user.role === "ADMIN";
  const isOwner = user.role === "USER" && complaint.userId === user.userId;
  const availableNextStatuses = NEXT_STATUS_OPTIONS[complaint.status] || [];

  return (
    <div className="page">
      <div className="complaint-header">
        <h2>#{complaint.id} — {complaint.title}</h2>
        <StatusBadge status={complaint.status} />
      </div>

      {actionMessage && <div className="alert alert-info">{actionMessage}</div>}

      <div className="card">
        <h3>Details</h3>
        <p><strong>Description:</strong> {complaint.description}</p>
        <div className="grid-2">
          <div><strong>Category:</strong> {complaint.categoryName || "Not yet categorized"}{complaint.subCategoryName ? ` (${complaint.subCategoryName})` : ""}</div>
          <div><strong>Priority:</strong> {complaint.priority}</div>
          <div><strong>Department:</strong> {complaint.departmentName || "—"}</div>
          <div><strong>Assigned to:</strong> {complaint.assignedStaffName || "Not yet assigned"}</div>
          <div><strong>Reference #:</strong> {complaint.referenceNumber || "—"}</div>
          <div><strong>Created by:</strong> {complaint.userName}</div>
          <div><strong>Created:</strong> {new Date(complaint.createdAt).toLocaleString()}</div>
          <div><strong>Due:</strong> {complaint.dueAt ? new Date(complaint.dueAt).toLocaleString() : "—"}</div>
        </div>
        {complaint.resolutionNotes && (
          <p><strong>Resolution notes:</strong> {complaint.resolutionNotes}</p>
        )}
      </div>

      <div className="card">
        <h3>AI Analysis</h3>
        {complaint.aiAnalysis ? (
          complaint.aiAnalysis.status === "SUCCESS" ? (
            <div className="ai-box">
              <p><strong>Suggested category:</strong> {complaint.aiAnalysis.suggestedCategory} {complaint.aiAnalysis.suggestedSubCategory ? `/ ${complaint.aiAnalysis.suggestedSubCategory}` : ""}</p>
              <p><strong>Suggested priority:</strong> {complaint.aiAnalysis.suggestedPriority}</p>
              <p><strong>AI summary:</strong> {complaint.aiAnalysis.summary}</p>
              <p className="muted">This is an AI-generated suggestion. Staff/admin can override it at any time.</p>
            </div>
          ) : (
            <div className="alert alert-warning">
              AI analysis was unavailable for this complaint (status: {complaint.aiAnalysis.status}).
              The complaint was still created successfully and can be categorized manually.
            </div>
          )
        ) : (
          <p className="muted">No AI analysis available yet.</p>
        )}
      </div>

      {related.length > 0 && (
        <div className="card">
          <h3>Possibly Related Complaints</h3>
          <p className="muted">Found using text-similarity within the same category. Not automatically merged - human confirmation required.</p>
          <ul>
            {related.map((r) => (
              <li key={r.complaintId}>
                <a href={`/complaints/${r.complaintId}`}>#{r.complaintId} — {r.title}</a> (similarity: {(r.similarityScore * 100).toFixed(0)}%)
              </li>
            ))}
          </ul>
        </div>
      )}

      {canManage && (
        <div className="card">
          <h3>Staff/Admin Actions</h3>

          <form className="inline-form" onSubmit={handleAssign}>
            <label>Assign to staff:</label>
            <select value={selectedStaffId} required onChange={(e) => setSelectedStaffId(e.target.value)}>
              <option value="">-- Select staff --</option>
              {staffList.map((s) => <option key={s.id} value={s.id}>{s.name} ({s.departmentName || "no dept"})</option>)}
            </select>
            <button className="btn btn-secondary" type="submit">Assign</button>
          </form>

          {availableNextStatuses.length > 0 && (
            <form className="inline-form" onSubmit={handleStatusUpdate}>
              <label>Change status to:</label>
              <select value={nextStatus} required onChange={(e) => setNextStatus(e.target.value)}>
                <option value="">-- Select --</option>
                {availableNextStatuses.map((s) => <option key={s} value={s}>{s}</option>)}
              </select>
              <input placeholder="Optional note" value={statusNote} onChange={(e) => setStatusNote(e.target.value)} />
              <button className="btn btn-secondary" type="submit">Update Status</button>
            </form>
          )}

          {(complaint.status === "IN_PROGRESS" || complaint.status === "ESCALATED") && (
            <>
              <button className="btn btn-link" type="button" onClick={handleGetSuggestion} disabled={suggestionLoading}>
                {suggestionLoading ? "Getting AI suggestion..." : "Get AI resolution suggestion"}
              </button>
              {suggestion && <div className="ai-box"><strong>AI suggestion:</strong> {suggestion} <span className="muted">(Suggestion only — you make the final decision.)</span></div>}

              <form className="form" onSubmit={handleResolve}>
                <label>Resolution notes *</label>
                <textarea required rows={3} value={resolutionNotes} onChange={(e) => setResolutionNotes(e.target.value)} />
                <button className="btn btn-primary" type="submit">Mark as Resolved</button>
              </form>
            </>
          )}
        </div>
      )}

      {isOwner && complaint.status === "RESOLVED" && (
        <div className="card">
          <h3>Confirm Resolution &amp; Give Feedback</h3>
          <form className="form" onSubmit={handleFeedback}>
            <label>Rating (1-5)</label>
            <select value={rating} onChange={(e) => setRating(Number(e.target.value))}>
              {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{n}</option>)}
            </select>
            <label>Comment (optional)</label>
            <textarea rows={3} value={comment} onChange={(e) => setComment(e.target.value)} />
            <button className="btn btn-primary" type="submit">Submit Feedback</button>
          </form>
        </div>
      )}

      <div className="card">
        <h3>History</h3>
        {history.length === 0 ? <p className="muted">No history yet.</p> : (
          <ul className="history-list">
            {history.map((h, i) => (
              <li key={i}>
                <strong>{h.fromStatus || "CREATED"} → {h.toStatus}</strong> by {h.changedByName} on {new Date(h.changedAt).toLocaleString()}
                {h.note && <div className="muted">{h.note}</div>}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
