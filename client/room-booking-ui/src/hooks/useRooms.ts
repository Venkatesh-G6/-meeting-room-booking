import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getAllRooms,
  createRoom,
  updateRoom,
  disableRoom,
  checkAvailability,
} from "../api";
import type { CreateRoomRequest } from "../types";

export const roomsQueryKey = (page: number, size: number) => [
  "rooms",
  page,
  size,
];

export function useRooms(page: number, size: number) {
  return useQuery({
    queryKey: roomsQueryKey(page, size),
    queryFn: async () => (await getAllRooms(page, size)).data,
  });
}

export function useCreateRoom() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateRoomRequest) => createRoom(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}

export function useUpdateRoom() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: CreateRoomRequest }) =>
      updateRoom(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}

export function useDisableRoom() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => disableRoom(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
    },
  });
}

export function useCheckAvailability() {
  return useMutation({
    mutationFn: (params: {
      date: string;
      startTime: string;
      endTime: string;
      minCapacity?: number;
    }) => checkAvailability(params),
  });
}
