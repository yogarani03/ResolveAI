import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  function homeLink() {
    if (!user) return "/";
    if (user.role === "ADMIN") return "/admin";
    if (user.role === "STAFF") return "/staff";
    return "/dashboard";
  }

  return (
    <nav className="navbar">
      <Link to={homeLink()} className="navbar-brand">ResolveAI</Link>
      <div className="navbar-links">
        {user ? (
          <>
            {user.role === "USER" && (
              <>
                <Link to="/dashboard">Dashboard</Link>
                <Link to="/complaints/new">New Complaint</Link>
                <Link to="/complaints">My Complaints</Link>
                <Link to="/notifications">Notifications</Link>
              </>
            )}
            {user.role === "STAFF" && (
              <>
                <Link to="/staff">Dashboard</Link>
                <Link to="/complaints">Assigned Complaints</Link>
              </>
            )}
            {user.role === "ADMIN" && (
              <>
                <Link to="/admin">Dashboard</Link>
                <Link to="/complaints">All Complaints</Link>
                <Link to="/admin/users">Users</Link>
                <Link to="/admin/categories">Categories</Link>
                <Link to="/admin/departments">Departments</Link>
              </>
            )}
            <span className="navbar-user">{user.name} ({user.role})</span>
            <button className="btn btn-link" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
