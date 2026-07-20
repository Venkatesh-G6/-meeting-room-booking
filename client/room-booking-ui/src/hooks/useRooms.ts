import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import toast from "react-hot-toast";
import {
  getAllRooms,
  getRoomById,
  createRoom,
  updateRoom,
  disableRoom,
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

export function useRoom(id: number) {
  return useQuery({
    queryKey: ["room", id],
    queryFn: async () => (await getRoomById(String(id))).data,
    enabled: id > 0,
  });
}

export function useCreateRoom() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateRoomRequest) => createRoom(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
      toast.success("Room created successfully");
    },
    onError: (error: string) => {
      toast.error(error);
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
      toast.success("Room updated successfully");
    },
    onError: (error: string) => {
      toast.error(error);
    },
  });
}

export function useDisableRoom() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => disableRoom(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["rooms"] });
      toast.success("Room disabled successfully");
    },
    onError: (error: string) => {
      toast.error(error);
    },
  });
}
