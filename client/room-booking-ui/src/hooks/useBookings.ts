import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import {
  getAllBookings,
  createBooking,
  cancelBooking,
  getMyBookings,
} from "../api";
import type { CreateBookingRequest } from "../types";

export const bookingsQueryKey = (page: number, size: number) => [
  "bookings",
  page,
  size,
];

export function useBookings(page: number, size: number) {
  return useQuery({
    queryKey: bookingsQueryKey(page, size),
    queryFn: async () => (await getAllBookings(page, size)).data,
  });
}

export function useMyBookings(email: string) {
  return useQuery({
    queryKey: ["myBookings", email],
    queryFn: async () => (await getMyBookings(email)).data,
    enabled: !!email,
  });
}

export function useCreateBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBookingRequest) => createBooking(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
      toast.success("Room booked successfully");
    },
    onError: (error: string) => {
      toast.error(error);
    },
  });
}

export function useCancelBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => cancelBooking(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      toast.success("Booking cancelled successfully");
    },
    onError: (error: string) => {
      toast.error(error);
    },
  });
}
