import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { complaintApi, referenceApi, getErrorMessage } from "../services/api";

export default function CreateComplaint() {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [departmentId, setDepartmentId] = useState("");
  const [referenceNumber, setReferenceNumber] = useState("");
  const [categories, setCategories] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    referenceApi.categories().then((res) => setCategories(res.data)).catch(() => {});
    referenceApi.departments().then((res) => setDepartments(res.data)).catch(() => {});
  }, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldErrors([]);
    setSubmitting(true);
    try {
      const res = await complaintApi.create({
        title,
        description,
        categoryId: categoryId || null,
        departmentId: departmentId || null,
        referenceNumber: referenceNumber || null,
      });
      navigate(`/complaints/${res.data.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
      if (err.response && err.response.data && err.response.data.details) {
        setFieldErrors(err.response.data.details);
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <h2>Create a Complaint</h2>
      <form className="card form" onSubmit={handleSubmit}>
        {error && (
          <div className="alert alert-error">
            {error}
            {fieldErrors.length > 0 && (
              <ul>{fieldErrors.map((d, i) => <li key={i}>{d}</li>)}</ul>
            )}
          </div>
        )}

        <label htmlFor="title">Title *</label>
        <input id="title" required value={title} onChange={(e) => setTitle(e.target.value)}
               placeholder="Brief summary of the issue" />

        <label htmlFor="description">Description *</label>
        <textarea id="description" required rows={6} value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe what happened in detail. Our AI assistant will suggest a category, priority and summary automatically." />

        <label htmlFor="category">Category (optional - AI will suggest one if left blank)</label>
        <select id="category" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">-- Let AI suggest --</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>{c.name}{c.subCategory ? ` / ${c.subCategory}` : ""}</option>
          ))}
        </select>

        <label htmlFor="department">Department (optional)</label>
        <select id="department" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
          <option value="">-- None --</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>{d.name}</option>
          ))}
        </select>

        <label htmlFor="ref">Order / Reference number (optional)</label>
        <input id="ref" value={referenceNumber} onChange={(e) => setReferenceNumber(e.target.value)} />

        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? "Submitting..." : "Submit Complaint"}
        </button>
      </form>
    </div>
  );
}
