import { BrowserRouter, Routes, Route } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import Dashboard from "./pages/Dashboard";
import Rooms from "./pages/Rooms";
import Bookings from "./pages/Bookings";
import Availability from "./pages/Availability";
import AuditLogs from "./pages/AuditLogs";
import BotSimulator from "./pages/BotSimulator";
import Login from "./pages/Login";
import { ErrorBoundary, ProtectedRoute } from "./components/common";
import { AuthProvider } from "./context/AuthContext";

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <ErrorBoundary>
                <Routes>
                  <Route path="/" element={<Dashboard />} />
                  <Route path="/rooms" element={<ProtectedRoute requireAdmin><Rooms /></ProtectedRoute>} />
                  <Route path="/bookings" element={<ProtectedRoute requireAdmin><Bookings /></ProtectedRoute>} />
                  <Route path="/availability" element={<Availability />} />
                  <Route path="/audit-logs" element={<ProtectedRoute requireAdmin><AuditLogs /></ProtectedRoute>} />
                  <Route path="/bot-simulator" element={<BotSimulator />} />
                </Routes>
              </ErrorBoundary>
            </ProtectedRoute>
          }
        />
      </Routes>
      </AuthProvider>
      <Toaster position="top-right" />
    </BrowserRouter>
  );
}

export default App;
