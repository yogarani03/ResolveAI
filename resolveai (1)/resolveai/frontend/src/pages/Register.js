import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    const result = await register(name, email, password);
    setSubmitting(false);
    if (!result.success) {
      setError(result.message);
      return;
    }
    navigate("/dashboard");
  }

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Create your account</h2>
        {error && <div className="alert alert-error">{error}</div>}

        <label htmlFor="name">Full name</label>
        <input id="name" value={name} required onChange={(e) => setName(e.target.value)} />

        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} required onChange={(e) => setEmail(e.target.value)} />

        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={password} required minLength={6}
               onChange={(e) => setPassword(e.target.value)} />

        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? "Creating account..." : "Register"}
        </button>

        <p className="auth-switch">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
}
