import { http, HttpResponse } from 'msw'
import dayjs from 'dayjs'

const tomorrow = dayjs().add(1, 'day')
const yesterday = dayjs().subtract(1, 'day')

const mockRooms = [
  {
    id: '1',
    roomName: 'Meeting Room A',
    roomType: 'MEETING',
    capacity: 10,
    location: 'Floor 1',
    active: true,
    createdAt: '2025-01-01T00:00:00',
    updatedAt: '2025-01-01T00:00:00',
  },
  {
    id: '2',
    roomName: 'Pod 1',
    roomType: 'POD',
    capacity: 4,
    location: 'Floor 2',
    active: true,
    createdAt: '2025-01-01T00:00:00',
    updatedAt: '2025-01-01T00:00:00',
  },
]

const mockBookings = [
  {
    id: '1',
    roomId: '1',
    roomName: 'Meeting Room A',
    bookedBy: 'admin@company.com',
    title: 'Team Standup',
    attendeeCount: 5,
    startTime: tomorrow.format('YYYY-MM-DD') + 'T10:00:00',
    endTime: tomorrow.format('YYYY-MM-DD') + 'T11:00:00',
    status: 'CONFIRMED',
    createdAt: '2025-01-01T00:00:00',
  },
  {
    id: '2',
    roomId: '2',
    roomName: 'Pod 1',
    bookedBy: 'admin@company.com',
    title: 'Client Call',
    attendeeCount: 2,
    startTime: yesterday.format('YYYY-MM-DD') + 'T14:00:00',
    endTime: yesterday.format('YYYY-MM-DD') + 'T15:00:00',
    status: 'CANCELLED',
    createdAt: '2025-01-01T00:00:00',
  },
]

const mockAuditLogs = [
  {
    id: 1,
    actorEmail: 'admin@company.com',
    action: 'ROOM_CREATED',
    entityType: 'Room',
    entityId: '1',
    metaJson: '{"roomName":"Meeting Room A"}',
    createdAt: '2025-01-01T10:00:00',
  },
  {
    id: 2,
    actorEmail: 'admin@company.com',
    action: 'BOOKING_CREATED',
    entityType: 'Booking',
    entityId: '1',
    metaJson: '{"title":"Team Standup"}',
    createdAt: '2025-01-01T11:00:00',
  },
  {
    id: 3,
    actorEmail: 'admin@company.com',
    action: 'BOOKING_CANCELLED',
    entityType: 'Booking',
    entityId: '2',
    metaJson: '{"title":"Client Call"}',
    createdAt: '2025-01-01T12:00:00',
  },
]

const pagedResponse = <T>(content: T[]) => ({
  content,
  pageNumber: 0,
  pageSize: 10,
  totalElements: content.length,
  totalPages: 1,
  last: true,
})

const apiResponse = <T>(data: T, message = 'Success') => ({
  success: true,
  message,
  data,
  timestamp: new Date().toISOString(),
})

export const handlers = [
  // ── Rooms ──────────────────────────────────

  http.get('http://localhost:8080/api/v1/rooms', () => {
    return HttpResponse.json(apiResponse(pagedResponse(mockRooms)))
  }),

  http.post('http://localhost:8080/api/v1/rooms', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>
    const newRoom = {
      id: '3',
      roomName: body.roomName ?? 'New Room',
      roomType: body.roomType ?? 'MEETING',
      capacity: body.capacity ?? 10,
      location: body.location ?? 'Floor 1',
      active: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    return HttpResponse.json(apiResponse(newRoom, 'Room created'), { status: 201 })
  }),

  http.put('http://localhost:8080/api/v1/rooms/:id', async ({ request, params }) => {
    const body = await request.json() as Record<string, unknown>
    const updated = {
      ...mockRooms.find(r => r.id === params.id),
      ...body,
      updatedAt: new Date().toISOString(),
    }
    return HttpResponse.json(apiResponse(updated, 'Room updated'))
  }),

  http.patch('http://localhost:8080/api/v1/rooms/:id/disable', ({ params }) => {
    const room = mockRooms.find(r => r.id === params.id)
    const disabled = { ...room, active: false, updatedAt: new Date().toISOString() }
    return HttpResponse.json(apiResponse(disabled, 'Room disabled'))
  }),

  http.get('http://localhost:8080/api/v1/rooms/available', () => {
    return HttpResponse.json(apiResponse({
      date: tomorrow.format('YYYY-MM-DD'),
      startTime: '10:00',
      endTime: '11:00',
      availableRooms: [mockRooms[0]],
      totalAvailable: 1,
    }))
  }),

  // ── Bookings ───────────────────────────────

  http.get('http://localhost:8080/api/v1/bookings', () => {
    return HttpResponse.json(apiResponse(pagedResponse(mockBookings)))
  }),

  http.post('http://localhost:8080/api/v1/bookings', async ({ request }) => {
    const body = await request.json() as Record<string, unknown>
    const newBooking = {
      id: '3',
      roomId: body.roomId ?? '1',
      roomName: 'Meeting Room A',
      bookedBy: 'admin@company.com',
      title: body.title ?? 'New Booking',
      attendeeCount: body.attendeeCount ?? 1,
      startTime: body.startTime ?? tomorrow.format('YYYY-MM-DD') + 'T10:00:00',
      endTime: body.endTime ?? tomorrow.format('YYYY-MM-DD') + 'T11:00:00',
      status: 'CONFIRMED',
      createdAt: new Date().toISOString(),
    }
    return HttpResponse.json(apiResponse(newBooking, 'Booking created'), { status: 201 })
  }),

  http.delete('http://localhost:8080/api/v1/bookings/:id', ({ params }) => {
    const booking = mockBookings.find(b => b.id === params.id)
    const cancelled = { ...booking, status: 'CANCELLED' }
    return HttpResponse.json(apiResponse(cancelled, 'Booking cancelled'))
  }),

  http.get('http://localhost:8080/api/v1/bookings/my', () => {
    return HttpResponse.json(apiResponse(mockBookings))
  }),

  // ── Audit Logs ─────────────────────────────

  http.get('http://localhost:8080/api/v1/audit-logs', () => {
    return HttpResponse.json(apiResponse(pagedResponse(mockAuditLogs)))
  }),
]
