export interface Employee {
  id: number
  name: string
  email: string
  department: string
}

export interface Room {
  id: number
  roomName: string
  capacity: number
  location: string
  status: 'AVAILABLE' | 'NA'
}

export interface Booking {
  id: number
  roomId: number
  roomName: string
  employeeId: number
  employeeName: string
  employeeEmail: string
  title: string
  startTime: string
  endTime: string
  status: 'CONFIRMED' | 'CANCELLED'
  createdAt: string
}

export interface AvailabilityResponse {
  available: boolean
  roomId: number
  roomName: string
  date: string
  requestedStart: string
  requestedEnd: string
  conflictingBooking: Booking | null
  suggestedStartTime: string | null
  suggestedEndTime: string | null
  message: string
}

export interface TodayBookingsResponse {
  roomId: number
  roomName: string
  location: string
  bookings: Booking[]
  fullyAvailable: boolean
}

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  timestamp: string
}
