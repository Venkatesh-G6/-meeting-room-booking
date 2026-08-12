import { useQuery } from "@tanstack/react-query";
import { getAllRooms, getAllEmployees } from "../api";

export function useRooms() {
  return useQuery({
    queryKey: ["rooms"],
    queryFn: async () => (await getAllRooms()).data,
  });
}

export function useEmployees() {
  return useQuery({
    queryKey: ["employees"],
    queryFn: async () => (await getAllEmployees()).data,
  });
}
