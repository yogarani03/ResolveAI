import React from "react";

const COLORS = {
  OPEN: "#6b7280",
  ASSIGNED: "#2563eb",
  IN_PROGRESS: "#d97706",
  ESCALATED: "#dc2626",
  RESOLVED: "#16a34a",
  CLOSED: "#4b5563",
  REOPENED: "#9333ea",
};

export default function StatusBadge({ status }) {
  const color = COLORS[status] || "#6b7280";
  return (
    <span className="status-badge" style={{ backgroundColor: color }}>
      {status}
    </span>
  );
}
