import React from "react";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import Navbar from "./components/Navbar";

import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Unauthorized from "./pages/Unauthorized";
import RedirectAfterLogin from "./pages/RedirectAfterLogin";

import UserDashboard from "./pages/UserDashboard";
import StaffDashboard from "./pages/StaffDashboard";
import AdminDashboard from "./pages/AdminDashboard";

import CreateComplaint from "./pages/CreateComplaint";
import ComplaintList from "./pages/ComplaintList";
import ComplaintDetails from "./pages/ComplaintDetails";

import Notifications from "./pages/Notifications";
import Profile from "./pages/Profile";

import AdminUsers from "./pages/AdminUsers";
import AdminCategories from "./pages/AdminCategories";
import AdminDepartments from "./pages/AdminDepartments";

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <main className="main-content">
          <Routes>
            {/* Public */}
            <Route path="/" element={<Landing />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/unauthorized" element={<Unauthorized />} />
            <Route path="/redirect-after-login" element={<RedirectAfterLogin />} />

            {/* User */}
            <Route path="/dashboard" element={
              <ProtectedRoute allowedRoles={["USER"]}><UserDashboard /></ProtectedRoute>
            } />
            <Route path="/complaints/new" element={
              <ProtectedRoute allowedRoles={["USER"]}><CreateComplaint /></ProtectedRoute>
            } />

            {/* Staff */}
            <Route path="/staff" element={
              <ProtectedRoute allowedRoles={["STAFF"]}><StaffDashboard /></ProtectedRoute>
            } />

            {/* Admin */}
            <Route path="/admin" element={
              <ProtectedRoute allowedRoles={["ADMIN"]}><AdminDashboard /></ProtectedRoute>
            } />
            <Route path="/admin/users" element={
              <ProtectedRoute allowedRoles={["ADMIN"]}><AdminUsers /></ProtectedRoute>
            } />
            <Route path="/admin/categories" element={
              <ProtectedRoute allowedRoles={["ADMIN"]}><AdminCategories /></ProtectedRoute>
            } />
            <Route path="/admin/departments" element={
              <ProtectedRoute allowedRoles={["ADMIN"]}><AdminDepartments /></ProtectedRoute>
            } />

            {/* Shared across all authenticated roles */}
            <Route path="/complaints" element={
              <ProtectedRoute allowedRoles={["USER", "STAFF", "ADMIN"]}><ComplaintList /></ProtectedRoute>
            } />
            <Route path="/complaints/:id" element={
              <ProtectedRoute allowedRoles={["USER", "STAFF", "ADMIN"]}><ComplaintDetails /></ProtectedRoute>
            } />
            <Route path="/notifications" element={
              <ProtectedRoute allowedRoles={["USER", "STAFF", "ADMIN"]}><Notifications /></ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute allowedRoles={["USER", "STAFF", "ADMIN"]}><Profile /></ProtectedRoute>
            } />

            {/* Fallback */}
            <Route path="*" element={<Unauthorized />} />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}
