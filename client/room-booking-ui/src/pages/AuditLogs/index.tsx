import { useState, useMemo } from "react";
import { Loader2, X, Eye } from "lucide-react";
import { Layout } from "../../components/layout";
import { PageHeader, Pagination } from "../../components/common";
import { useAuditLogs } from "../../hooks";
import type { AuditLog } from "../../types";
import { formatDateTime } from "../../utils/dateUtils";

const actionColors: Record<string, string> = {
  BOOKING_CREATED: "bg-green-100 text-green-700 border border-green-200",
  BOOKING_CANCELLED: "bg-red-100 text-red-700 border border-red-200",
  ROOM_CREATED: "bg-blue-100 text-blue-700 border border-blue-200",
  ROOM_UPDATED: "bg-yellow-100 text-yellow-700 border border-yellow-200",
  ROOM_DISABLED: "bg-gray-100 text-gray-700 border border-gray-200",
};

const actionOptions = [
  "ALL",
  "BOOKING_CREATED",
  "BOOKING_CANCELLED",
  "ROOM_CREATED",
  "ROOM_UPDATED",
  "ROOM_DISABLED",
];

export default function AuditLogs() {
  const [actionFilter, setActionFilter] = useState("ALL");
  const [searchEmail, setSearchEmail] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 20;
  const [detailLog, setDetailLog] = useState<AuditLog | null>(null);

  const { data, isLoading } = useAuditLogs(currentPage, pageSize);
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const filteredLogs = useMemo(() => {
    const logs = data?.content ?? [];
    return logs.filter((log) => {
      if (actionFilter !== "ALL" && log.action !== actionFilter) return false;
      if (
        searchEmail &&
        !log.actorEmail.toLowerCase().includes(searchEmail.toLowerCase())
      )
        return false;
      return true;
    });
  }, [data, actionFilter, searchEmail]);

  function clearFilters() {
    setActionFilter("ALL");
    setSearchEmail("");
  }

  const parsedMeta = useMemo(() => {
    if (!detailLog) return {};
    try {
      return JSON.parse(detailLog.metaJson);
    } catch {
      return { raw: detailLog.metaJson };
    }
  }, [detailLog]);

  return (
    <Layout title="Audit Logs">
      <PageHeader
        title="Audit Logs"
        subtitle="Complete action history"
      />

      <div className="bg-white rounded-lg shadow p-4 mb-4 flex flex-wrap items-center gap-3">
        <select
          value={actionFilter}
          onChange={(e) => setActionFilter(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        >
          {actionOptions.map((opt) => (
            <option key={opt} value={opt}>
              {opt === "ALL" ? "All Actions" : opt}
            </option>
          ))}
        </select>

        <input
          type="text"
          placeholder="Search by actor email..."
          value={searchEmail}
          onChange={(e) => setSearchEmail(e.target.value)}
          className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-56"
        />

        <button
          onClick={clearFilters}
          className="text-sm text-gray-600 hover:text-gray-800 font-medium px-3 py-2 transition-colors"
        >
          Clear Filters
        </button>
      </div>

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center h-48">
            <Loader2 className="w-6 h-6 text-blue-500 animate-spin" />
          </div>
        ) : filteredLogs.length === 0 ? (
          <div className="flex items-center justify-center h-48 text-sm text-gray-400">
            No audit logs found
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 text-left">
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Timestamp
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actor Email
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Action
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Entity Type
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Entity ID
                </th>
                <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Details
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredLogs.map((log) => (
                <tr key={log.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {formatDateTime(log.createdAt)}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {log.actorEmail}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        actionColors[log.action] ||
                        "bg-gray-100 text-gray-700 border border-gray-200"
                      }`}
                    >
                      {log.action}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {log.entityType}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600 font-mono text-xs">
                    {log.entityId
                      ? log.entityId.length > 12
                        ? `${log.entityId.slice(0, 8)}...`
                        : log.entityId
                      : "-"}
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => setDetailLog(log)}
                      className="flex items-center gap-1 text-blue-600 hover:text-blue-700 text-sm font-medium transition-colors"
                    >
                      <Eye className="w-4 h-4" />
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {!isLoading && totalPages > 0 && (
        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
        />
      )}

      {detailLog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-800">
                Audit Log Details
              </h3>
              <button
                onClick={() => setDetailLog(null)}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="px-6 py-4 space-y-3">
              <div>
                <span className="text-xs font-medium text-gray-500 uppercase">
                  Action
                </span>
                <p className="text-sm text-gray-800 mt-0.5">
                  {detailLog.action}
                </p>
              </div>
              <div>
                <span className="text-xs font-medium text-gray-500 uppercase">
                  Actor
                </span>
                <p className="text-sm text-gray-800 mt-0.5">
                  {detailLog.actorEmail}
                </p>
              </div>
              <div>
                <span className="text-xs font-medium text-gray-500 uppercase">
                  Entity
                </span>
                <p className="text-sm text-gray-800 mt-0.5">
                  {detailLog.entityType} / {detailLog.entityId}
                </p>
              </div>
              <div>
                <span className="text-xs font-medium text-gray-500 uppercase">
                  Timestamp
                </span>
                <p className="text-sm text-gray-800 mt-0.5">
                  {formatDateTime(detailLog.createdAt)}
                </p>
              </div>
              <div>
                <span className="text-xs font-medium text-gray-500 uppercase">
                  Metadata
                </span>
                <div className="mt-1 bg-gray-50 rounded-lg p-3 space-y-1.5">
                  {Object.entries(parsedMeta).map(([key, value]) => (
                    <div
                      key={key}
                      className="flex items-start justify-between text-sm"
                    >
                      <span className="font-medium text-gray-600">{key}</span>
                      <span className="text-gray-800 text-right ml-3 break-all">
                        {String(value)}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex justify-end px-6 py-4 border-t border-gray-200">
              <button
                onClick={() => setDetailLog(null)}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
