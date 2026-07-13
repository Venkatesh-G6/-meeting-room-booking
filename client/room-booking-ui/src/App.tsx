import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import Dashboard from "./pages/Dashboard";
import Rooms from "./pages/Rooms";
import Bookings from "./pages/Bookings";
import Availability from "./pages/Availability";
import AuditLogs from "./pages/AuditLogs";
import { ErrorBoundary, ProtectedRoute } from "./components/common";
import { AuthProvider } from "./context/AuthContext";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <ProtectedRoute>
            <BrowserRouter>
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/rooms" element={<Rooms />} />
                <Route path="/bookings" element={<Bookings />} />
                <Route path="/availability" element={<Availability />} />
                <Route path="/audit-logs" element={<AuditLogs />} />
              </Routes>
              <Toaster position="top-right" />
            </BrowserRouter>
          </ProtectedRoute>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}

export default App;
