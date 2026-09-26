import React, { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);
    const result = await login(email, password);
    setSubmitting(false);
    if (!result.success) {
      setError(result.message);
      return;
    }
    navigate("/redirect-after-login");
  }

  return (
    <div className="auth-page">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>Login</h2>
        {error && <div className="alert alert-error">{error}</div>}
        <label htmlFor="email">Email</label>
        <input id="email" type="email" value={email} required
               onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />

        <label htmlFor="password">Password</label>
        <input id="password" type="password" value={password} required
               onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />

        <button className="btn btn-primary" type="submit" disabled={submitting}>
          {submitting ? "Logging in..." : "Login"}
        </button>

        <p className="auth-switch">
          Don't have an account? <Link to="/register">Register</Link>
        </p>

        <div className="demo-accounts">
          <strong>Demo accounts:</strong>
          <div>Admin: admin@resolveai.com / Admin@123</div>
          <div>Staff: staff1@resolveai.com / Staff@123</div>
          <div>User: user1@resolveai.com / User@123</div>
        </div>
      </form>
    </div>
  );
}
