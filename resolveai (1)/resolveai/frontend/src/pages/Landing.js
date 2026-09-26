import React from "react";
import { Link } from "react-router-dom";

export default function Landing() {
  return (
    <div className="landing">
      <h1>ResolveAI</h1>
      <p className="tagline">Intelligent Complaint Resolution Platform</p>
      <p>
        Submit complaints, get AI-assisted categorization and summaries, track
        investigation and resolution end-to-end, and give feedback once resolved.
      </p>
      <div className="landing-actions">
        <Link className="btn btn-primary" to="/register">Get Started</Link>
        <Link className="btn btn-secondary" to="/login">Login</Link>
      </div>
    </div>
  );
}
