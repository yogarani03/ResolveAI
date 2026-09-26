import React, { useEffect, useState } from "react";
import { adminApi, getErrorMessage } from "../services/api";

export default function AdminDepartments() {
  const [departments, setDepartments] = useState([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function load() {
    adminApi.listDepartments().then((res) => setDepartments(res.data)).catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await adminApi.createDepartment({ name, description: description || null });
      setName(""); setDescription("");
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <h2>Departments</h2>
      <div className="card">
        <h3>Add Department</h3>
        {error && <div className="alert alert-error">{error}</div>}
        <form className="inline-form" onSubmit={handleSubmit}>
          <input placeholder="Department name" required value={name} onChange={(e) => setName(e.target.value)} />
          <input placeholder="Description (optional)" value={description} onChange={(e) => setDescription(e.target.value)} />
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Adding..." : "Add Department"}
          </button>
        </form>
      </div>
      <div className="card">
        <h3>Existing Departments</h3>
        <table className="table">
          <thead><tr><th>ID</th><th>Name</th><th>Description</th></tr></thead>
          <tbody>
            {departments.map((d) => (
              <tr key={d.id}><td>{d.id}</td><td>{d.name}</td><td>{d.description || "—"}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
