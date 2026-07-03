import apiClient from "./client";
import type {
  ApiResponse,
  PagedResponse,
  Room,
  AvailabilityResponse,
  CreateRoomRequest,
} from "../types";

export function getAllRooms(
  page: number,
  size: number
): Promise<ApiResponse<PagedResponse<Room>>> {
  return apiClient.get("/rooms", { params: { page, size } });
}

export function getRoomById(id: string): Promise<ApiResponse<Room>> {
  return apiClient.get(`/rooms/${id}`);
}

export function createRoom(
  data: CreateRoomRequest
): Promise<ApiResponse<Room>> {
  return apiClient.post("/rooms", data);
}

export function updateRoom(
  id: string,
  data: CreateRoomRequest
): Promise<ApiResponse<Room>> {
  return apiClient.put(`/rooms/${id}`, data);
}

export function disableRoom(id: string): Promise<ApiResponse<void>> {
  return apiClient.patch(`/rooms/${id}/disable`);
}

export function checkAvailability(params: {
  date: string;
  startTime: string;
  endTime: string;
  minCapacity?: number;
}): Promise<ApiResponse<AvailabilityResponse>> {
  return apiClient.get("/rooms/available", { params });
}
