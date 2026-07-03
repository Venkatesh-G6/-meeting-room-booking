import apiClient from "./client";
import type {
  ApiResponse,
  PagedResponse,
  AuditLog,
} from "../types";

export function getAuditLogs(
  page: number,
  size: number
): Promise<ApiResponse<PagedResponse<AuditLog>>> {
  return apiClient.get("/audit-logs", { params: { page, size } });
}

export function getEntityAuditLogs(
  entityType: string,
  entityId: string
): Promise<ApiResponse<AuditLog[]>> {
  return apiClient.get(`/audit-logs/entity/${entityType}/${entityId}`);
}
