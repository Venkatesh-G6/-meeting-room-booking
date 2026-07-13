import { useQuery } from "@tanstack/react-query";
import { getAuditLogs, getEntityAuditLogs } from "../api";

export function useAuditLogs(page: number, size: number) {
  return useQuery({
    queryKey: ["auditLogs", page, size],
    queryFn: async () => (await getAuditLogs(page, size)).data,
  });
}

export function useEntityAuditLogs(entityType: string, entityId: string) {
  return useQuery({
    queryKey: ["auditLogs", "entity", entityType, entityId],
    queryFn: async () => (await getEntityAuditLogs(entityType, entityId)).data,
    enabled: !!entityType && !!entityId,
  });
}
