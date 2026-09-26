import React, { useEffect, useState } from "react";
import { adminApi, getErrorMessage } from "../services/api";

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [departmentId, setDepartmentId] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function load() {
    adminApi.listUsers().then((res) => setUsers(res.data)).catch((err) => setError(getErrorMessage(err)));
    adminApi.listDepartments().then((res) => setDepartments(res.data)).catch(() => {});
  }

  useEffect(load, []);

  async function handleCreateStaff(e) {
    e.preventDefault();
    setError("");
    setMessage("");
    setSubmitting(true);
    try {
      await adminApi.createStaff({ name, email, password, departmentId: departmentId || null });
      setMessage(`Staff account created for ${name}.`);
      setName(""); setEmail(""); setPassword(""); setDepartmentId("");
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <h2>Users &amp; Staff</h2>

      <div className="card">
        <h3>Create Staff Account</h3>
        {error && <div className="alert alert-error">{error}</div>}
        {message && <div className="alert alert-info">{message}</div>}
        <form className="inline-form" onSubmit={handleCreateStaff}>
          <input placeholder="Full name" required value={name} onChange={(e) => setName(e.target.value)} />
          <input placeholder="Email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          <input placeholder="Temp password" type="password" required minLength={6} value={password} onChange={(e) => setPassword(e.target.value)} />
          <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
            <option value="">-- Department --</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Creating..." : "Create Staff"}
          </button>
        </form>
      </div>

      <div className="card">
        <h3>All Accounts</h3>
        <table className="table">
          <thead><tr><th>ID</th><th>Name</th><th>Email</th><th>Role</th><th>Department</th></tr></thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td>{u.role}</td>
                <td>{u.departmentName || "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
