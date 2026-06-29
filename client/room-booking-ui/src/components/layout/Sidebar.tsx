import { NavLink } from "react-router-dom";
import {
  Building2,
  LayoutDashboard,
  DoorOpen,
  CalendarDays,
  Search,
} from "lucide-react";

const navItems = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard },
  { to: "/rooms", label: "Rooms", icon: DoorOpen },
  { to: "/bookings", label: "Bookings", icon: CalendarDays },
  { to: "/availability", label: "Availability", icon: Search },
];

export default function Sidebar() {
  return (
    <aside className="w-64 h-screen bg-gray-900 text-white flex flex-col fixed left-0 top-0">
      <div className="flex items-center gap-2 px-6 py-5 border-b border-gray-700">
        <Building2 className="w-6 h-6 text-blue-400" />
        <span className="text-lg font-semibold">Room Booking</span>
      </div>

      <nav className="flex-1 py-4">
        {navItems.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/"}
            className={({ isActive }) =>
              `flex items-center gap-3 px-6 py-3 text-sm font-medium transition-colors ${
                isActive
                  ? "bg-blue-600 text-white"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            <Icon className="w-5 h-5" />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
