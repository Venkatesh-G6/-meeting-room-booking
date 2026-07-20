import { useContext } from "react";
import { AuthContext, type AuthUser, type AuthContextType } from "./AuthContext";

export function getCachedToken(): string | null {
  const stored = sessionStorage.getItem("roombooking_user");
  if (stored) {
    try {
      const user = JSON.parse(stored) as AuthUser;
      return user.token;
    } catch (err) {
      console.error("Failed to parse stored user:", err);
    }
  }
  return null;
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}
