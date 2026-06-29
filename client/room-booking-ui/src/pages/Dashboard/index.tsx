import { useEffect, useState } from "react";
import {
  DoorOpen,
  CheckCircle,
  CalendarDays,
  Clock,
  Loader2,
} from "lucide-react";
import dayjs from "dayjs";
import { Layout } from "../../components/layout";
import { Badge, StatCard } from "../../components/common";
import { getAllRooms, getAllBookings } from "../../api";
import type { Room, Booking } from "../../types";

export default function Dashboard() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchData() {
      try {
        const [roomsRes, bookingsRes] = await Promise.all([
          getAllRooms(),
          getAllBookings(),
        ]);
        setRooms(roomsRes.data);
        setBookings(bookingsRes.data);
      } catch (err) {
        setError(err as string);
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  if (loading) {
    return (
      <Layout title="Dashboard">
        <div className="flex items-center justify-center h-64">
          <Loader2 className="w-8 h-8 text-blue-500 animate-spin" />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout title="Dashboard">
        <div className="flex items-center justify-center h-64">
          <p className="text-red-500">{error}</p>
        </div>
      </Layout>
    );
  }

  const today = dayjs().format("YYYY-MM-DD");

  const totalRooms = rooms.length;
  const activeRooms = rooms.filter((r) => r.active).length;
  const bookingsToday = bookings.filter((b) =>
    dayjs(b.startTime).format("YYYY-MM-DD") === today
  ).length;
  const confirmedBookings = bookings.filter(
    (b) => b.status === "CONFIRMED"
  ).length;

  const recentBookings = [...bookings]
    .sort((a, b) => (a.createdAt < b.createdAt ? 1 : -1))
    .slice(0, 5);

  return (
    <Layout title="Dashboard">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard
          title="Total Rooms"
          value={totalRooms}
          icon={DoorOpen}
          color="bg-blue-500"
        />
        <StatCard
          title="Active Rooms"
          value={activeRooms}
          icon={CheckCircle}
          color="bg-green-500"
        />
        <StatCard
          title="Total Bookings Today"
          value={bookingsToday}
          icon={CalendarDays}
          color="bg-purple-500"
        />
        <StatCard
          title="Confirmed Bookings"
          value={confirmedBookings}
          icon={Clock}
          color="bg-orange-500"
        />
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <h3 className="text-lg font-semibold text-gray-800 px-6 py-4 border-b border-gray-200">
          Recent Bookings
        </h3>
        <table className="w-full">
          <thead>
            <tr className="bg-gray-50 text-left">
              <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                Room
              </th>
              <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                Booked By
              </th>
              <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                Date
              </th>
              <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                Time
              </th>
              <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                Status
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {recentBookings.map((booking) => (
              <tr key={booking.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-800">
                  {booking.roomName}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {booking.bookedBy}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {dayjs(booking.startTime).format("YYYY-MM-DD")}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {dayjs(booking.startTime).format("HH:mm")} -{" "}
                  {dayjs(booking.endTime).format("HH:mm")}
                </td>
                <td className="px-6 py-4">
                  <Badge status={booking.status} />
                </td>
              </tr>
            ))}
            {recentBookings.length === 0 && (
              <tr>
                <td
                  colSpan={5}
                  className="px-6 py-8 text-center text-sm text-gray-400"
                >
                  No recent bookings
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}
