import { useQuery } from "@tanstack/react-query";
import { checkAvailability } from "../api";

export function useAvailability(
  params: { roomId: number; date: string; startTime: string; endTime: string },
  enabled: boolean
) {
  return useQuery({
    queryKey: ["availability", params],
    queryFn: async () =>
      (await checkAvailability(params.roomId, params.date, params.startTime, params.endTime)).data,
    enabled,
    staleTime: 30 * 1000,
  });
}
