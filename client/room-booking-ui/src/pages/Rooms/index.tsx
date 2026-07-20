import { useState } from "react";
import {
  Plus,
  Pencil,
  Ban,
  Loader2,
  X,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Layout } from "../../components/layout";
import { Badge, PageHeader, Pagination } from "../../components/common";
import { useRooms, useCreateRoom, useUpdateRoom, useDisableRoom } from "../../hooks";
import { roomSchema, type RoomFormData } from "../../utils/validationSchemas";
import type { Room } from "../../types";

export default function Rooms() {
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 10;

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors, isSubmitting }
  } = useForm<RoomFormData>({
    resolver: zodResolver(roomSchema),
    defaultValues: {
      roomType: "MEETING",
      capacity: 1
    }
  });

  const { data, isLoading } = useRooms(currentPage, pageSize);
  const createRoomMutation = useCreateRoom();
  const updateRoomMutation = useUpdateRoom();
  const disableRoomMutation = useDisableRoom();

  const rooms = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  function openAddModal() {
    setEditingRoom(null);
    reset();
    setModalOpen(true);
  }

  function openEditModal(room: Room) {
    setEditingRoom(room);
    setValue("roomName", room.roomName);
    setValue("roomType", room.roomType);
    setValue("capacity", room.capacity);
    setValue("location", room.location);
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setEditingRoom(null);
    reset();
  }

  function handleSave(data: RoomFormData) {
    if (editingRoom) {
      updateRoomMutation.mutate({ id: editingRoom.id, data });
    } else {
      createRoomMutation.mutate(data);
    }
    closeModal();
    setCurrentPage(0);
  }

  function handleDisable(room: Room) {
    if (!window.confirm(`Disable ${room.roomName}?`)) return;
    disableRoomMutation.mutate(room.id);
    setCurrentPage(0);
  }

  return (
    <Layout title="Room Management">
      <PageHeader
        title="Room Management"
        subtitle="Manage all meeting rooms"
        action={
          <button
            onClick={openAddModal}
            className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-4 h-4" />
            Add Room
          </button>
        }
      />

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {isLoading ? (
          <div className="flex items-center justify-center h-48">
            <Loader2 className="w-6 h-6 text-blue-500 animate-spin" />
          </div>
        ) : (
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 text-left">
                <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Name
                </th>
                <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Type
                </th>
                <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Capacity
                </th>
                <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Location
                </th>
                <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {rooms.map((room) => (
                <tr key={room.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 text-sm font-medium text-gray-800">
                    {room.roomName}
                  </td>
                  <td className="px-6 py-4">
                    <Badge status={room.roomType} />
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {room.capacity}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {room.location}
                  </td>
                  <td className="px-6 py-4">
                    <Badge status={room.active} />
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => openEditModal(room)}
                        className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
                        title="Edit"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      {room.active && (
                        <button
                          onClick={() => handleDisable(room)}
                          className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded transition-colors"
                          title="Disable"
                        >
                          <Ban className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {rooms.length === 0 && (
                <tr>
                  <td
                    colSpan={6}
                    className="px-6 py-8 text-center text-sm text-gray-400"
                  >
                    No rooms found
                  </td>
                </tr>
              )}
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

      {modalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-lg w-full max-w-md mx-4">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <h3 className="text-lg font-semibold text-gray-800">
                {editingRoom ? "Edit Room" : "Add Room"}
              </h3>
              <button
                onClick={closeModal}
                className="text-gray-400 hover:text-gray-600 transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form id="room-form" onSubmit={handleSubmit(handleSave)} className="px-6 py-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Room Name
                </label>
                <input
                  type="text"
                  {...register("roomName")}
                  className={`w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.roomName ? "border-red-400" : "border-gray-300"
                  }`}
                  placeholder="e.g. Conference Room A"
                />
                {errors.roomName && (
                  <p className="text-red-500 text-sm mt-1">{errors.roomName.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Room Type
                </label>
                <select
                  {...register("roomType")}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="MEETING">MEETING</option>
                  <option value="TRAINING">TRAINING</option>
                  <option value="POD">POD</option>
                </select>
                {errors.roomType && (
                  <p className="text-red-500 text-sm mt-1">{errors.roomType.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Capacity
                </label>
                <input
                  type="number"
                  min={1}
                  {...register("capacity", { valueAsNumber: true })}
                  className={`w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.capacity ? "border-red-400" : "border-gray-300"
                  }`}
                />
                {errors.capacity && (
                  <p className="text-red-500 text-sm mt-1">{errors.capacity.message}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Location
                </label>
                <input
                  type="text"
                  {...register("location")}
                  className={`w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                    errors.location ? "border-red-400" : "border-gray-300"
                  }`}
                  placeholder="e.g. Floor 3"
                />
                {errors.location && (
                  <p className="text-red-500 text-sm mt-1">{errors.location.message}</p>
                )}
              </div>
            </form>

            <div className="flex justify-end gap-3 px-6 py-4 border-t border-gray-200">
              <button
                type="button"
                onClick={closeModal}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                form="room-form"
                disabled={isSubmitting}
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {isSubmitting && <Loader2 className="w-4 h-4 animate-spin" />}
                {isSubmitting ? "Saving..." : "Save"}
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
