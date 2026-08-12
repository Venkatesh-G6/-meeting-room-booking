import api from './client'
import type { 
  Employee, Room,
  AvailabilityResponse,
  Booking,
  TodayFacilitySchedulesResponse,
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
    '/facility-schedules/availability',
    { params: { roomId, date, 
        startTime, endTime } })

export const createSchedule = (data: {
  roomId: number
  employeeId: number
  title: string
  date: string
  startTime: string
  endTime: string
}) =>
  api.post<any, ApiResponse<Booking>>(
    '/facility-schedules', data)

export const getTodaySchedules = () =>
  api.get<any, 
    ApiResponse<TodayFacilitySchedulesResponse[]>>(
    '/facility-schedules/today')

export const getRecentSchedules = (
  days = 5) =>
  api.get<any, ApiResponse<Booking[]>>(
    '/facility-schedules/recent', 
    { params: { days } })

export const cancelSchedule = (
  id: number, employeeId: number) =>
  api.delete<any, ApiResponse<void>>(
    `/facility-schedules/${id}`,
    { params: { employeeId } })
