import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import {
  createBooking,
  cancelBooking,
  getTodayBookings,
  getRecentBookings,
  type CreateBookingPayload,
} from "../api";

export function useTodayBookings() {
  return useQuery({
    queryKey: ["bookings", "today"],
    queryFn: async () => (await getTodayBookings()).data,
  });
}

export function useRecentBookings(days: number) {
  return useQuery({
    queryKey: ["bookings", "recent", days],
    queryFn: async () => (await getRecentBookings(days)).data,
  });
}

export function useCreateBooking() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateBookingPayload) => createBooking(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
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
    mutationFn: ({ id, employeeId }: { id: number; employeeId: number }) =>
      cancelBooking(id, employeeId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bookings"] });
      toast.success("Booking cancelled successfully");
    },
    onError: (error: string) => {
      toast.error(error);
    },
  });
}
