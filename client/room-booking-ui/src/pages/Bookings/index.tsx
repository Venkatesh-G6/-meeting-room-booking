import { useEffect, useState, useMemo } from "react";
import {
  Loader2,
  CalendarX,
  Ban,
} from "lucide-react";
import dayjs from "dayjs";
import { toast } from "react-hot-toast";
import { Layout } from "../../components/layout";
import { Badge, PageHeader, Pagination } from "../../components/common";
import { getAllBookings, cancelBooking } from "../../api";
import type { Booking } from "../../types";
import { formatDate, formatTime } from "../../utils/dateUtils";

export default function Bookings() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchEmail, setSearchEmail] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [dateFilter, setDateFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  async function fetchBookings() {
    try {
      const res = await getAllBookings(currentPage, pageSize);
      setBookings(res.data.content);
      setTotalPages(res.data.totalPages);
      setTotalElements(res.data.totalElements);
    } catch (err) {
      toast.error(err as string);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchBookings();
  }, [currentPage]);

  async function handleCancel(booking: Booking) {
    if (!window.confirm(`Cancel booking for ${booking.roomName}?`)) return;
    try {
      await cancelBooking(booking.id);
      toast.success("Booking cancelled successfully");
      setCurrentPage(0);
      await fetchBookings();
    } catch (err) {
      toast.error(err as string);
    }
  }

  function clearFilters() {
    setSearchEmail("");
    setStatusFilter("ALL");
    setDateFilter("");
  }

  const filteredBookings = useMemo(() => {
    return bookings.filter((b) => {
      if (searchEmail && !b.bookedBy.toLowerCase().includes(searchEmail.toLowerCase()))
        return false;
      if (statusFilter !== "ALL" && b.status !== statusFilter) return false;
      if (dateFilter && dayjs(b.startTime).format("YYYY-MM-DD") !== dateFilter)
        return false;
      return true;
    });
  }, [bookings, searchEmail, statusFilter, dateFilter]);

  return (
    <Layout title="Bookings">
      <PageHeader title="All Bookings" subtitle="View and manage all room bookings" />

      <div className="bg-white rounded-lg shadow p-4 mb-4 flex flex-wrap items-center gap-3">
        <input
          type="text"
          placeholder="Search by email..."
          value={searchEmail}
          onChange={(e) => setSearchEmail(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-56"
        />

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          <option value="ALL">All Status</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="CANCELLED">Cancelled</option>
        </select>

        <input
          type="date"
          value={dateFilter}
          onChange={(e) => setDateFilter(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />

        <button
          onClick={clearFilters}
          className="text-sm text-gray-600 hover:text-gray-800 font-medium px-3 py-2 transition-colors"
        >
          Clear Filters
        </button>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-48">
            <Loader2 className="w-6 h-6 text-blue-500 animate-spin" />
          </div>
        ) : filteredBookings.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-48 text-gray-400">
            <CalendarX className="w-10 h-10 mb-2" />
            <p className="text-sm">No bookings found</p>
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 text-left">
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Room Name</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Booked By</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Title</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Start</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">End</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Attendees</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredBookings.map((booking) => (
                <tr key={booking.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-600">{booking.id}</td>
                  <td className="px-4 py-3 text-sm font-medium text-gray-800">{booking.roomName}</td>
                  <td className="px-4 py-3 text-sm text-gray-600">{booking.bookedBy}</td>
                  <td className="px-4 py-3 text-sm text-gray-600">{booking.title}</td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatDate(booking.startTime)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatTime(booking.startTime)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatTime(booking.endTime)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">{booking.attendeeCount}</td>
                  <td className="px-4 py-3">
                    <Badge status={booking.status} />
                  </td>
                  <td className="px-4 py-3">
                    {booking.status === "CONFIRMED" && (
                      <button
                        onClick={() => handleCancel(booking)}
                        className="flex items-center gap-1 text-red-600 hover:text-red-700 text-sm font-medium transition-colors"
                      >
                        <Ban className="w-4 h-4" />
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {!loading && totalPages > 0 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}
    </Layout>
  );
}
