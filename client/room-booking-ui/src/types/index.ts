export interface Room {
  id: string
  roomName: string
  roomType: 'MEETING' | 'TRAINING' | 'POD'
  capacity: number
  location: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface Booking {
  id: string
  roomId: string
  roomName: string
  bookedBy: string
  title: string
  attendeeCount: number
  startTime: string
  endTime: string
  status: 'CONFIRMED' | 'CANCELLED'
  createdAt: string
}

export interface AvailabilityResponse {
  date: string
  startTime: string
  endTime: string
  availableRooms: Room[]
  totalAvailable: number
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}

export interface PagedResponse<T> {
  content: T[]
  pageNumber: number
  pageSize: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface CreateRoomRequest {
  roomName: string
  roomType: 'MEETING' | 'TRAINING' | 'POD'
  capacity: number
  location: string
}

export interface CreateBookingRequest {
  roomId: string
  title: string
  attendeeCount: number
  startTime: string
  endTime: string
}

export interface AuditLog {
  id: number
  actorEmail: string
  action: string
  entityType: string
  entityId: string
  metaJson: string
  createdAt: string
}
