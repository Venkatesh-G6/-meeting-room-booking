import { NavLink } from "react-router-dom";
import {
  Building2,
  LayoutDashboard,
  DoorOpen,
  CalendarDays,
  Search,
  ClipboardList,
  Bot,
  LogOut,
} from "lucide-react";
import { useAuth } from "../../context/auth-utils";

const isDev = import.meta.env.DEV;

export default function Sidebar() {
  const { isAdmin, logout } = useAuth();

  return (
    <aside className="w-64 h-screen bg-gray-900 text-white flex flex-col fixed left-0 top-0">
      <div className="flex items-center gap-2 px-6 py-5 border-b border-gray-700">
        <Building2 className="w-6 h-6 text-blue-400" />
        <span className="text-lg font-semibold">MeetSpace</span>
      </div>

      <nav className="flex-1 py-4">
        <NavLink
          to="/"
          end
          className={({ isActive }) =>
            `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
              isActive
                ? "bg-blue-600 text-white"
                : "text-gray-400 hover:text-white hover:bg-gray-800"
            }`
          }
        >
          <LayoutDashboard className="w-5 h-5" />
          Dashboard
        </NavLink>

        {isAdmin && (
          <NavLink
            to="/rooms"
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            <DoorOpen className="w-5 h-5" />
            Rooms
          </NavLink>
        )}

        {isAdmin && (
          <NavLink
            to="/bookings"
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            <CalendarDays className="w-5 h-5" />
            Bookings
          </NavLink>
        )}

        <NavLink
          to="/availability"
          className={({ isActive }) =>
            `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
              isActive
                ? "bg-blue-600 text-white"
                : "text-gray-400 hover:text-white hover:bg-gray-800"
            }`
          }
        >
          <Search className="w-5 h-5" />
          Availability
        </NavLink>

        {isAdmin && (
          <NavLink
            to="/audit-logs"
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            <ClipboardList className="w-5 h-5" />
            Audit Logs
          </NavLink>
        )}
      </nav>

      {isDev && (
        <nav className="py-2 border-t border-gray-700">
          <p className="px-6 py-2 text-xs font-semibold text-gray-500 uppercase">Dev Tools</p>
          <NavLink
            to="/bot-simulator"
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            <Bot className="w-5 h-5" />
            Bot Simulator
          </NavLink>
        </nav>
      )}

      <div className="p-4 border-t border-gray-700">
        <button
          onClick={logout}
          className="flex items-center gap-3 px-6 py-3 text-sm font-medium text-gray-400 hover:text-white hover:bg-gray-800 w-full rounded-lg transition-colors"
        >
          <LogOut className="w-5 h-5" />
          Sign Out
        </button>
      </div>
    </aside>
  );
}
