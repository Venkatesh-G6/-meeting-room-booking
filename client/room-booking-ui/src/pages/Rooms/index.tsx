import { useEffect, useState } from "react";
import {
  Plus,
  Pencil,
  Ban,
  Loader2,
  X,
} from "lucide-react";
import { toast } from "react-hot-toast";
import { Layout } from "../../components/layout";
import { Badge, PageHeader, Pagination } from "../../components/common";
import { getAllRooms, createRoom, updateRoom, disableRoom } from "../../api";
import type { Room, CreateRoomRequest } from "../../types";

const emptyForm: CreateRoomRequest = {
  roomName: "",
  roomType: "MEETING",
  capacity: 1,
  location: "",
};

export default function Rooms() {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [form, setForm] = useState<CreateRoomRequest>(emptyForm);
  const [saving, setSaving] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const pageSize = 10;

  async function fetchRooms() {
    try {
      const res = await getAllRooms(currentPage, pageSize);
      setRooms(res.data.content);
      setTotalPages(res.data.totalPages);
      setTotalElements(res.data.totalElements);
    } catch (err) {
      toast.error(err as string);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    fetchRooms();
  }, [currentPage]);

  function openAddModal() {
    setEditingRoom(null);
    setForm(emptyForm);
    setModalOpen(true);
  }

  function openEditModal(room: Room) {
    setEditingRoom(room);
    setForm({
      roomName: room.roomName,
      roomType: room.roomType,
      capacity: room.capacity,
      location: room.location,
    });
    setModalOpen(true);
  }

  function closeModal() {
    setModalOpen(false);
    setEditingRoom(null);
    setForm(emptyForm);
  }

  async function handleSave() {
    if (!form.roomName.trim() || !form.location.trim()) {
      toast.error("Room name and location are required");
      return;
    }
    setSaving(true);
    try {
      if (editingRoom) {
        await updateRoom(editingRoom.id, form);
        toast.success("Room updated successfully");
      } else {
        await createRoom(form);
        toast.success("Room created successfully");
      }
      closeModal();
      setCurrentPage(0);
      await fetchRooms();
    } catch (err) {
      toast.error(err as string);
    } finally {
      setSaving(false);
    }
  }

  async function handleDisable(room: Room) {
    if (!window.confirm(`Disable ${room.roomName}?`)) return;
    try {
      await disableRoom(room.id);
      toast.success("Room disabled successfully");
      setCurrentPage(0);
      await fetchRooms();
    } catch (err) {
      toast.error(err as string);
    }
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
        {loading ? (
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

      {!loading && totalPages > 0 && (
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

            <div className="px-6 py-4 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Room Name
                </label>
                <input
                  type="text"
                  value={form.roomName}
                  onChange={(e) =>
                    setForm({ ...form, roomName: e.target.value })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. Conference Room A"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Room Type
                </label>
                <select
                  value={form.roomType}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      roomType: e.target.value as CreateRoomRequest["roomType"],
                    })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="MEETING">MEETING</option>
                  <option value="TRAINING">TRAINING</option>
                  <option value="POD">POD</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Capacity
                </label>
                <input
                  type="number"
                  min={1}
                  value={form.capacity}
                  onChange={(e) =>
                    setForm({ ...form, capacity: Number(e.target.value) })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Location
                </label>
                <input
                  type="text"
                  value={form.location}
                  onChange={(e) =>
                    setForm({ ...form, location: e.target.value })
                  }
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="e.g. Floor 3"
                />
              </div>
            </div>

            <div className="flex justify-end gap-3 px-6 py-4 border-t border-gray-200">
              <button
                onClick={closeModal}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSave}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {saving && <Loader2 className="w-4 h-4 animate-spin" />}
                Save
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}
