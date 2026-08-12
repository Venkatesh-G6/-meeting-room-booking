import apiClient from "./client";
import type { ApiResponse, Room, Employee } from "../types";

export function getAllRooms(): Promise<ApiResponse<Room[]>> {
  return apiClient.get("/rooms");
}

export function getAllEmployees(): Promise<ApiResponse<Employee[]>> {
  return apiClient.get("/employees");
}
