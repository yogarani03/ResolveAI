import React from "react";
import { Link } from "react-router-dom";

export default function Unauthorized() {
  return (
    <div className="empty-state">
      <h2>Access denied</h2>
      <p>You do not have permission to view this page.</p>
      <Link className="btn btn-primary" to="/">Go home</Link>
    </div>
  );
}
