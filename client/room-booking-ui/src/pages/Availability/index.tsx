import { useState } from "react";
import {
  Loader2,
  Search,
  CalendarX,
  X,
  MapPin,
  Users,
} from "lucide-react";
import dayjs from "dayjs";
import { toast } from "react-hot-toast";
import { Layout } from "../../components/layout";
import { Badge, PageHeader } from "../../components/common";
import { checkAvailability, createBooking } from "../../api";
import type { Room, CreateBookingRequest } from "../../types";
import {
  formatDateForApi,
  formatTimeForApi,
  formatTime,
  isPast,
} from "../../utils/dateUtils";

export default function Availability() {
  const [date, setDate] = useState("");
  const [startTime, setStartTime] = useState("");
  const [endTime, setEndTime] = useState("");
  const [minCapacity, setMinCapacity] = useState(1);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [availableRooms, setAvailableRooms] = useState<Room[]>([]);
  const [bookingRoom, setBookingRoom] = useState<Room | null>(null);
  const [bookingForm, setBookingForm] = useState<CreateBookingRequest>({
    roomId: "",
    title: "",
    attendeeCount: 1,
    startTime: "",
    endTime: "",
  });
  const [saving, setSaving] = useState(false);

  function validate(): boolean {
    const newErrors: Record<string, string> = {};

    if (!date) {
      newErrors.date = "Date is required";
    } else if (isPast(date)) {
      newErrors.date = "Date cannot be in the past";
    }

    if (!startTime) newErrors.startTime = "Start time is required";
    if (!endTime) newErrors.endTime = "End time is required";

    if (startTime && endTime && endTime <= startTime) {
      newErrors.endTime = "End time must be after start time";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  async function handleSearch() {
    if (!validate()) return;
    setLoading(true);
    try {
      const res = await checkAvailability({
        date: formatDateForApi(new Date(date)),
        startTime: formatTimeForApi(startTime),
        endTime: formatTimeForApi(endTime),
        minCapacity,
      });
      setAvailableRooms(res.data.availableRooms);
      setSearched(true);
    } catch (err) {
      toast.error(err as string);
    } finally {
      setLoading(false);
    }
  }

  function openBookingModal(room: Room) {
    setBookingRoom(room);
    setBookingForm({
      roomId: room.id,
      title: "",
      attendeeCount: 1,
      startTime: `${date}T${startTime}`,
      endTime: `${date}T${endTime}`,
    });
  }

  function closeBookingModal() {
    setBookingRoom(null);
    setBookingForm({
      roomId: "",
      title: "",
      attendeeCount: 1,
      startTime: "",
      endTime: "",
    });
  }

  async function handleConfirmBooking() {
    if (!bookingRoom) return;
    if (!bookingForm.title.trim()) {
      toast.error("Meeting title is required");
      return;
    }
    setSaving(true);
    try {
      await createBooking(bookingForm);
      toast.success("Booking confirmed successfully!");
      closeBookingModal();
    } catch (err) {
      toast.error(err as string);
    } finally {
      setSaving(false);
    }
  }

  const today = dayjs().format("YYYY-MM-DD");

  return (
    <Layout title="Availability">
      <PageHeader
        title="Check Room Availability"
        subtitle="Find available rooms for your meeting"
      />

      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Date
            </label>
            <input
              type="date"
              min={today}
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {errors.date && (
              <p className="text-xs text-red-500 mt-1">{errors.date}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Start Time
            </label>
            <input
              type="time"
              value={startTime}
              onChange={(e) => setStartTime(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {errors.startTime && (
              <p className="text-xs text-red-500 mt-1">{errors.startTime}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              End Time
            </label>
            <input
              type="time"
              value={endTime}
              onChange={(e) => setEndTime(e.target.value)}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {errors.endTime && (
              <p className="text-xs text-red-500 mt-1">{errors.endTime}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Min Capacity
            </label>
            <input
              type="number"
              min={1}
              value={minCapacity}
              onChange={(e) => setMinCapacity(Number(e.target.value))}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <button
          onClick={handleSearch}
          disabled={loading}
          className="w-full mt-4 flex items-center justify-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors disabled:opacity-50"
        >
          {loading ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Search className="w-4 h-4" />
          )}
          Check Availability
        </button>
      </div>

      {searched && (
        <div>
          {availableRooms.length > 0 ? (
            <>
              <h3 className="text-lg font-semibold text-green-600 mb-4">
                {availableRooms.length} room{availableRooms.length > 1 ? "s" : ""} available
              </h3>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {availableRooms.map((room) => (
                  <div
                    key={room.id}
                    className="bg-white rounded-lg shadow p-5 flex flex-col"
                  >
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <h4 className="text-base font-semibold text-gray-800">
                          {room.roomName}
                        </h4>
                        <div className="flex items-center gap-2 mt-1">
                          <Badge status={room.roomType} />
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-4 text-sm text-gray-600 mb-4">
                      <span className="flex items-center gap-1">
                        <Users className="w-4 h-4" />
                        {room.capacity}
                      </span>
                      <span className="flex items-center gap-1">
                        <MapPin className="w-4 h-4" />
                        {room.location}
                      </span>
                    </div>

                    <button
                      onClick={() => openBookingModal(room)}
                      className="w-full bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
                    >
                      Book Now
                    </button>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center justify-center h-48 text-gray-400">
              <CalendarX className="w-10 h-10 mb-2" />
              <p className="text-sm font-medium">No rooms available</p>
              <p className="text-xs mt-1">
                Try a different date or time slot
              </p>
            </div>
          )}
        </div>
      )}

      {bookingRoom && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-800">
                Book {bookingRoom.roomName}
              </h3>
              <button
                onClick={closeBookingModal}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="px-6 py-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Room
                </label>
                <input
                  type="text"
                  value={bookingRoom.roomName}
                  disabled
                  className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Meeting Title
                </label>
                <input
                  type="text"
                  value={bookingForm.title}
                  onChange={(e) =>
                    setBookingForm({ ...bookingForm, title: e.target.value })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. Team Standup"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Attendee Count
                </label>
                <input
                  type="number"
                  min={1}
                  max={bookingRoom.capacity}
                  value={bookingForm.attendeeCount}
                  onChange={(e) =>
                    setBookingForm({
                      ...bookingForm,
                      attendeeCount: Number(e.target.value),
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <p className="text-xs text-gray-400 mt-1">
                  Max capacity: {bookingRoom.capacity}
                </p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Start Time
                  </label>
                  <input
                    type="text"
                    value={formatTime(bookingForm.startTime)}
                    disabled
                    className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    End Time
                  </label>
                  <input
                    type="text"
                    value={formatTime(bookingForm.endTime)}
                    disabled
                    className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-500"
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end gap-3 px-6 py-4 border-t border-gray-200">
              <button
                onClick={closeBookingModal}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmBooking}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                Confirm Booking
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
