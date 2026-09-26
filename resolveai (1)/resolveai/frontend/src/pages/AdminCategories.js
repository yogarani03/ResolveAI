import React, { useEffect, useState } from "react";
import { adminApi, getErrorMessage } from "../services/api";

export default function AdminCategories() {
  const [categories, setCategories] = useState([]);
  const [name, setName] = useState("");
  const [subCategory, setSubCategory] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  function load() {
    adminApi.listCategories().then((res) => setCategories(res.data)).catch((err) => setError(getErrorMessage(err)));
  }

  useEffect(load, []);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    try {
      await adminApi.createCategory({ name, subCategory: subCategory || null });
      setName(""); setSubCategory("");
      load();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <h2>Complaint Categories</h2>
      <div className="card">
        <h3>Add Category</h3>
        {error && <div className="alert alert-error">{error}</div>}
        <form className="inline-form" onSubmit={handleSubmit}>
          <input placeholder="Category name (e.g. PAYMENT)" required value={name} onChange={(e) => setName(e.target.value)} />
          <input placeholder="Sub-category (optional)" value={subCategory} onChange={(e) => setSubCategory(e.target.value)} />
          <button className="btn btn-primary" type="submit" disabled={submitting}>
            {submitting ? "Adding..." : "Add Category"}
          </button>
        </form>
      </div>
      <div className="card">
        <h3>Existing Categories</h3>
        <table className="table">
          <thead><tr><th>ID</th><th>Name</th><th>Sub-category</th></tr></thead>
          <tbody>
            {categories.map((c) => (
              <tr key={c.id}><td>{c.id}</td><td>{c.name}</td><td>{c.subCategory || "—"}</td></tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
