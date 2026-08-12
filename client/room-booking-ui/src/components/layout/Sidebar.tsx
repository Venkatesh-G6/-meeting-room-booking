import { Building2, CalendarDays } from "lucide-react";

export default function Sidebar() {
  return (
    <aside className="w-64 h-screen bg-gray-900 text-white flex flex-col fixed left-0 top-0">
      <div className="flex items-center gap-2 px-6 py-5 border-b border-gray-700">
        <Building2 className="w-6 h-6 text-blue-400" />
        <span className="text-lg font-semibold">MeetSpace</span>
      </div>

      <nav className="flex-1 py-4">
        <div className="flex items-center gap-3 px-6 py-3 text-sm font-medium bg-blue-600 text-white">
          <CalendarDays className="w-5 h-5" />
          Bookings
        </div>
      </nav>
    </aside>
  );
}
