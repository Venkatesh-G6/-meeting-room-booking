import type { ReactNode } from "react";
import { Loader2, ShieldAlert } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { user, isLoading, error, retry } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="w-8 h-8 text-blue-500 animate-spin" />
          <p className="text-sm text-gray-500">Signing you in with Microsoft Teams...</p>
        </div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <div className="max-w-md w-full bg-white rounded-lg shadow p-8 text-center">
          <div className="flex justify-center mb-4">
            <ShieldAlert className="w-12 h-12 text-red-500" />
          </div>
          <h2 className="text-lg font-semibold text-gray-800 mb-2">
            Sign-in required
          </h2>
          <p className="text-sm text-gray-500 mb-6">
            {error ||
              "This app must be opened inside Microsoft Teams to sign in."}
          </p>
          <button
            onClick={retry}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
          >
            Retry Sign-in
          </button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
