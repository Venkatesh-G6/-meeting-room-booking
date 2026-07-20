import { useQuery } from "@tanstack/react-query";
import { checkAvailability } from "../api";

export function useAvailability(params: { date: string; startTime: string; endTime: string; minCapacity: number }, enabled: boolean) {
  return useQuery({
    queryKey: ["availability", params],
    queryFn: async () => (await checkAvailability(params)).data,
    enabled,
    staleTime: 30 * 1000,
  });
}
