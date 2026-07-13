import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
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

export function useMyBookings(bookedBy: string) {
  return useQuery({
    queryKey: ["bookings", "my", bookedBy],
    queryFn: async () => (await getMyBookings(bookedBy)).data,
    enabled: !!bookedBy,
  });
}

export function useCreateBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBookingRequest) => createBooking(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}

export function useCancelBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => cancelBooking(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}
