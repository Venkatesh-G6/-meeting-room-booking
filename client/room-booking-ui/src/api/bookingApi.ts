import api from './client'
import type { 
  Employee, Room,
  AvailabilityResponse,
  Booking,
  TodayBookingsResponse,
  ApiResponse
} from '../types'

export const getEmployees = () =>
  api.get<any, ApiResponse<Employee[]>>(
    '/employees')

export const getRooms = () =>
  api.get<any, ApiResponse<Room[]>>(
    '/rooms')

export const checkAvailability = (
  roomId: number,
  date: string,
  startTime: string,
  endTime: string
) =>
  api.get<any, 
    ApiResponse<AvailabilityResponse>>(
    '/bookings/availability',
    { params: { roomId, date, 
        startTime, endTime } })

export const createBooking = (data: {
  roomId: number
  employeeId: number
  title: string
  date: string
  startTime: string
  endTime: string
}) =>
  api.post<any, ApiResponse<Booking>>(
    '/bookings', data)

export const getTodayBookings = () =>
  api.get<any, 
    ApiResponse<TodayBookingsResponse[]>>(
    '/bookings/today')

export const getRecentBookings = (
  days = 5) =>
  api.get<any, ApiResponse<Booking[]>>(
    '/bookings/recent', 
    { params: { days } })

export const cancelBooking = (
  id: number, employeeId: number) =>
  api.delete<any, ApiResponse<void>>(
    `/bookings/${id}`,
    { params: { employeeId } })
