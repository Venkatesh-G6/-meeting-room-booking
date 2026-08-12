import apiClient from "./client";
import type {
  ApiResponse,
  Booking,
  AvailabilityResponse,
  TodayBookingsResponse,
} from "../types";

export interface CreateBookingPayload {
  roomId: number;
  employeeId: number;
  title: string;
  date: string;
  startTime: string;
  endTime: string;
}

export function checkAvailability(
  roomId: number,
  date: string,
  startTime: string,
  endTime: string
): Promise<ApiResponse<AvailabilityResponse>> {
  return apiClient.get("/bookings/availability", {
    params: { roomId, date, startTime, endTime },
  });
}

export function createBooking(
  data: CreateBookingPayload
): Promise<ApiResponse<Booking>> {
  return apiClient.post("/bookings", data);
}

export function getTodayBookings(): Promise<ApiResponse<TodayBookingsResponse[]>> {
  return apiClient.get("/bookings/today");
}

export function getRecentBookings(
  days: number
): Promise<ApiResponse<Booking[]>> {
  return apiClient.get("/bookings/recent", { params: { days } });
}

export function cancelBooking(
  id: number,
  employeeId: number
): Promise<ApiResponse<void>> {
  return apiClient.delete(`/bookings/${id}`, { params: { employeeId } });
}
