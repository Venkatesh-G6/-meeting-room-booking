import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Building2 } from "lucide-react";
import toast from "react-hot-toast";
import { useAuth } from "../../context/auth-utils";

export default function Login() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/");
    }
  }, [isAuthenticated, navigate]);

  function handleDevLogin() {
    login({
      email: "admin@company.com",
      displayName: "Admin User",
      role: "ADMIN",
      token: "dev-token"
    });
    navigate("/");
  }

  function handleMicrosoftLogin() {
    // TODO Phase 9: Initialize MSAL instance and call loginRedirect()
    // MSAL will handle token acquisition and redirect back with user profile
    toast("Microsoft login coming in Phase 9");
  }

  const isDev = import.meta.env.VITE_ENV === "dev";

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-600 to-blue-800 flex items-center justify-center px-4">
      <div className="bg-white rounded-lg shadow-xl p-8 w-full max-w-md">
        <div className="flex justify-center mb-6">
          <Building2 className="w-12 h-12 text-blue-600" />
        </div>
        <h1 className="text-2xl font-bold text-gray-800 text-center mb-2">
          Room Booking System
        </h1>
        <p className="text-gray-600 text-center mb-8">
          Book meeting rooms effortlessly
        </p>

        <button
          onClick={handleMicrosoftLogin}
          className="w-full flex items-center justify-center gap-3 bg-white text-blue-600 border border-blue-600 rounded-lg px-4 py-3 font-medium hover:bg-blue-50 transition-colors shadow-sm"
        >
          <svg width="20" height="20" viewBox="0 0 21 21" fill="none">
            <path d="M10.5 0C4.75 0 0 4.75 0 10.5S4.75 21 10.5 21 21 16.25 21 10.5 16.25 0 10.5 0Z" fill="#F3F3F3"/>
            <path d="M10.5 0C4.75 0 0 4.75 0 10.5S4.75 21 10.5 21V0Z" fill="#F35325"/>
            <path d="M10.5 0C16.25 0 21 4.75 21 10.5S16.25 21 10.5 21V0Z" fill="#7BCBA3"/>
            <path d="M10.5 10.5C4.75 10.5 0 15.25 0 21S4.75 31.5 10.5 31.5 21 26.75 21 21 16.25 10.5 10.5 10.5Z" fill="#00A4EF"/>
            <path d="M10.5 10.5C16.25 10.5 21 15.25 21 21S16.25 31.5 10.5 31.5 21 26.75 21 21 16.25 10.5 10.5 10.5Z" fill="#FFB900"/>
          </svg>
          Sign in with Microsoft
        </button>

        <div className="flex items-center my-6">
          <div className="flex-1 border-t border-gray-300"></div>
          <span className="px-4 text-gray-500 text-sm">or</span>
          <div className="flex-1 border-t border-gray-300"></div>
        </div>

        {isDev && (
          <button
            onClick={handleDevLogin}
            className="w-full border border-gray-300 text-gray-700 rounded-lg px-4 py-3 font-medium hover:bg-gray-50 transition-colors"
          >
            Dev Mode Login
          </button>
        )}
      </div>
    </div>
  );
}
