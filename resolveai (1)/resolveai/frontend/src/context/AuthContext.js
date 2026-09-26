import React, { createContext, useContext, useEffect, useState } from "react";
import { authApi, getErrorMessage } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Restore session from localStorage on first load.
  useEffect(() => {
    const storedUser = localStorage.getItem("resolveai_user");
    const storedToken = localStorage.getItem("resolveai_token");
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  function persistSession(authResponse) {
    const { token, ...userInfo } = authResponse;
    localStorage.setItem("resolveai_token", token);
    localStorage.setItem("resolveai_user", JSON.stringify(userInfo));
    setUser(userInfo);
  }

  async function login(email, password) {
    try {
      const res = await authApi.login({ email, password });
      persistSession(res.data);
      return { success: true };
    } catch (err) {
      return { success: false, message: getErrorMessage(err) };
    }
  }

  async function register(name, email, password) {
    try {
      const res = await authApi.register({ name, email, password });
      persistSession(res.data);
      return { success: true };
    } catch (err) {
      return { success: false, message: getErrorMessage(err) };
    }
  }

  function logout() {
    localStorage.removeItem("resolveai_token");
    localStorage.removeItem("resolveai_user");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
