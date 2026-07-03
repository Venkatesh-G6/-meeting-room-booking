import apiClient from "./client";
import type {
  ApiResponse,
  Booking,
  CreateBookingRequest,
  PagedResponse,
} from "../types";

export function getAllBookings(
  page: number,
  size: number
): Promise<ApiResponse<PagedResponse<Booking>>> {
  return apiClient.get("/bookings", { params: { page, size } });
}

export function getBookingById(id: string): Promise<ApiResponse<Booking>> {
  return apiClient.get(`/bookings/${id}`);
}

export function createBooking(
  data: CreateBookingRequest
): Promise<ApiResponse<Booking>> {
  return apiClient.post("/bookings", data);
}

export function cancelBooking(
  id: string
): Promise<ApiResponse<void>> {
  return apiClient.delete(`/bookings/${id}`);
}

export function getMyBookings(
  bookedBy: string
): Promise<ApiResponse<Booking[]>> {
  return apiClient.get("/bookings/my", {
    params: { bookedBy },
  });
}
