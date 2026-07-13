import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from "react";
import * as microsoftTeams from "@microsoft/teams-js";

interface AuthUser {
  displayName?: string;
  userPrincipalName?: string;
  token: string;
}

interface AuthContextValue {
  user: AuthUser | null;
  isLoading: boolean;
  error: string | null;
  isInTeams: boolean;
  getToken: () => Promise<string>;
  retry: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

// Module-level cache so apiClient (outside React) can read the latest token
let cachedToken: string | null = null;
export function getCachedToken(): string | null {
  return cachedToken;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isInTeams, setIsInTeams] = useState(false);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let cancelled = false;

    async function initialize() {
      setIsLoading(true);
      setError(null);
      try {
        await microsoftTeams.app.initialize();
        if (cancelled) return;
        setIsInTeams(true);

        const context = await microsoftTeams.app.getContext();
        const token = await microsoftTeams.authentication.getAuthToken();
        if (cancelled) return;

        cachedToken = token;
        setUser({
          displayName: context.user?.displayName,
          userPrincipalName: context.user?.userPrincipalName,
          token,
        });
      } catch (err) {
        if (cancelled) return;
        setIsInTeams(false);
        setError(
          err instanceof Error
            ? err.message
            : "Failed to authenticate with Microsoft Teams. This app must be opened inside Teams."
        );
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }

    initialize();
    return () => {
      cancelled = true;
    };
  }, [retryCount]);

  async function getToken(): Promise<string> {
    if (cachedToken) return cachedToken;
    const token = await microsoftTeams.authentication.getAuthToken();
    cachedToken = token;
    return token;
  }

  function retry() {
    setRetryCount((c) => c + 1);
  }

  return (
    <AuthContext.Provider
      value={{ user, isLoading, error, isInTeams, getToken, retry }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
