import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Small helper page: sends the freshly-logged-in user to the right dashboard for their role. */
export default function RedirectAfterLogin() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (user.role === "ADMIN") return <Navigate to="/admin" replace />;
  if (user.role === "STAFF") return <Navigate to="/staff" replace />;
  return <Navigate to="/dashboard" replace />;
}
