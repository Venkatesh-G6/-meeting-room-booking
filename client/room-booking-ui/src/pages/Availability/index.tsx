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
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Layout } from "../../components/layout";
import { Badge, PageHeader } from "../../components/common";
import { useAvailability, useCreateBooking } from "../../hooks";
import { availabilitySchema, bookingSchema, type AvailabilityFormData, type BookingFormData } from "../../utils/validationSchemas";
import type { Room } from "../../types";
import {
  formatDateForApi,
  formatTimeForApi,
  formatTime,
} from "../../utils/dateUtils";

export default function Availability() {
  const [searched, setSearched] = useState(false);
  const [bookingRoom, setBookingRoom] = useState<Room | null>(null);
  const [searchDate, setSearchDate] = useState("");
  const [searchStartTime, setSearchStartTime] = useState("");
  const [searchEndTime, setSearchEndTime] = useState("");

  const {
    register: registerSearch,
    handleSubmit: handleSearchSubmit,
    formState: { errors: searchErrors }
  } = useForm<AvailabilityFormData>({
    resolver: zodResolver(availabilitySchema),
    defaultValues: {
      minCapacity: 1
    }
  });

  const {
    register: registerBooking,
    handleSubmit: handleBookingSubmit,
    reset: resetBooking,
    setValue: setBookingValue,
    formState: { errors: bookingErrors, isSubmitting: bookingSubmitting }
  } = useForm<BookingFormData>({
    resolver: zodResolver(bookingSchema)
  });

  const createBookingMutation = useCreateBooking();

  const availabilityParams = {
    date: formatDateForApi(new Date(searchDate)),
    startTime: formatTimeForApi(searchStartTime),
    endTime: formatTimeForApi(searchEndTime),
    minCapacity: 1,
  };
  const { data: availabilityData, isLoading: availabilityLoading } = useAvailability(availabilityParams, searched);
  const availableRooms = availabilityData?.availableRooms ?? [];

  function onSearchSubmit(data: AvailabilityFormData) {
    setSearchDate(data.date);
    setSearchStartTime(data.startTime);
    setSearchEndTime(data.endTime);
    setSearched(true);
  }

  function openBookingModal(room: Room) {
    setBookingRoom(room);
    setBookingValue("roomId", Number(room.id));
    setBookingValue("bookedBy", "user@example.com");
    setBookingValue("startTime", `${searchDate}T${searchStartTime}`);
    setBookingValue("endTime", `${searchDate}T${searchEndTime}`);
  }

  function closeBookingModal() {
    setBookingRoom(null);
    resetBooking();
  }

  function onBookingSubmit(data: BookingFormData) {
    createBookingMutation.mutate({
      ...data,
      roomId: String(data.roomId)
    });
    closeBookingModal();
  }

  const today = dayjs().format("YYYY-MM-DD");

  return (
    <Layout title="Availability">
      <PageHeader
        title="Check Room Availability"
        subtitle="Find available rooms for your meeting"
      />

      <form onSubmit={handleSearchSubmit(onSearchSubmit)} className="bg-white rounded-lg shadow p-6 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Date
            </label>
            <input
              type="date"
              min={today}
              {...registerSearch("date")}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {searchErrors.date && (
              <p className="text-xs text-red-500 mt-1">{searchErrors.date.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Start Time
            </label>
            <input
              type="time"
              {...registerSearch("startTime")}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {searchErrors.startTime && (
              <p className="text-xs text-red-500 mt-1">{searchErrors.startTime.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              End Time
            </label>
            <input
              type="time"
              {...registerSearch("endTime")}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {searchErrors.endTime && (
              <p className="text-xs text-red-500 mt-1">{searchErrors.endTime.message}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Min Capacity
            </label>
            <input
              type="number"
              min={1}
              {...registerSearch("minCapacity", { valueAsNumber: true })}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            {searchErrors.minCapacity && (
              <p className="text-xs text-red-500 mt-1">{searchErrors.minCapacity.message}</p>
            )}
          </div>
        </div>

        <button
          type="submit"
          disabled={availabilityLoading}
          className="w-full mt-4 flex items-center justify-center gap-2 bg-blue-600 text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors disabled:opacity-50"
        >
          {availabilityLoading ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <Search className="w-4 h-4" />
          )}
          Check Availability
        </button>
      </form>

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

            <form id="booking-form" onSubmit={handleBookingSubmit(onBookingSubmit)} className="px-6 py-4 space-y-4">
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
                  {...registerBooking("title")}
                  className={`w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    bookingErrors.title ? "border-red-400" : "border-gray-300"
                  }`}
                  placeholder="e.g. Team Standup"
                />
                {bookingErrors.title && (
                  <p className="text-xs text-red-500 mt-1">{bookingErrors.title.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Attendee Count
                </label>
                <input
                  type="number"
                  min={1}
                  max={bookingRoom.capacity}
                  {...registerBooking("attendeeCount", { valueAsNumber: true })}
                  className={`w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    bookingErrors.attendeeCount ? "border-red-400" : "border-gray-300"
                  }`}
                />
                {bookingErrors.attendeeCount ? (
                  <p className="text-xs text-red-500 mt-1">{bookingErrors.attendeeCount.message}</p>
                ) : (
                  <p className="text-xs text-gray-400 mt-1">
                    Max capacity: {bookingRoom.capacity}
                  </p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Start Time
                  </label>
                  <input
                    type="text"
                    value={formatTime(`${searchDate}T${searchStartTime}`)}
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
                    value={formatTime(`${searchDate}T${searchEndTime}`)}
                    disabled
                    className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm bg-gray-50 text-gray-500"
                  />
                </div>
              </div>
            </form>

            <div className="flex justify-end gap-3 px-6 py-4 border-t border-gray-200">
              <button
                type="button"
                onClick={closeBookingModal}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                form="booking-form"
                disabled={bookingSubmitting}
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {bookingSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
                {bookingSubmitting ? "Saving..." : "Confirm Booking"}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
