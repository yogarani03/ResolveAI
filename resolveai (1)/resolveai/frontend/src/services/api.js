import axios from "axios";

// Base URL of the Spring Boot backend. Override with REACT_APP_API_URL if needed.
const API_BASE_URL = process.env.REACT_APP_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// Attach the JWT token (if present) to every outgoing request.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("resolveai_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Central place to handle auth failures: if the token is invalid/expired,
// log the user out and send them back to the login page.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("resolveai_token");
      localStorage.removeItem("resolveai_user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

// Extracts a readable error message from a backend ApiError response,
// falling back to a generic message for network/unexpected errors.
export function getErrorMessage(error) {
  if (error.response && error.response.data && error.response.data.message) {
    return error.response.data.message;
  }
  if (error.request) {
    return "Could not reach the server. Please check your connection and try again.";
  }
  return "Something went wrong. Please try again.";
}

// ---------------- Auth ----------------
export const authApi = {
  register: (data) => api.post("/auth/register", data),
  login: (data) => api.post("/auth/login", data),
};

// ---------------- Complaints ----------------
export const complaintApi = {
  create: (data) => api.post("/complaints", data),
  list: () => api.get("/complaints"),
  getById: (id) => api.get(`/complaints/${id}`),
  updateStatus: (id, data) => api.patch(`/complaints/${id}/status`, data),
  assign: (id, data) => api.post(`/complaints/${id}/assign`, data),
  resolve: (id, data) => api.post(`/complaints/${id}/resolve`, data),
  submitFeedback: (id, data) => api.post(`/complaints/${id}/feedback`, data),
  related: (id) => api.get(`/complaints/${id}/related`),
  history: (id) => api.get(`/complaints/${id}/history`),
  resolutionSuggestion: (id) => api.get(`/complaints/${id}/resolution-suggestion`),
};

// ---------------- Reference data ----------------
export const referenceApi = {
  categories: () => api.get("/categories"),
  departments: () => api.get("/departments"),
  staff: () => api.get("/staff"),
};

// ---------------- Notifications ----------------
export const notificationApi = {
  list: () => api.get("/notifications"),
};

// ---------------- Dashboard ----------------
export const dashboardApi = {
  admin: () => api.get("/dashboard/admin"),
  staff: () => api.get("/dashboard/staff"),
  user: () => api.get("/dashboard/user"),
};

// ---------------- Admin ----------------
export const adminApi = {
  listUsers: () => api.get("/admin/users"),
  listStaff: () => api.get("/admin/staff"),
  createStaff: (data) => api.post("/admin/staff", data),
  listDepartments: () => api.get("/admin/departments"),
  createDepartment: (data) => api.post("/admin/departments", data),
  listCategories: () => api.get("/admin/categories"),
  createCategory: (data) => api.post("/admin/categories", data),
};

export default api;
