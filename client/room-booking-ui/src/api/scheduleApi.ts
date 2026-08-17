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
    '/meets/availability',
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
    '/meets', data)

export const getTodaySchedules = () =>
  api.get<any, 
    ApiResponse<TodayFacilitySchedulesResponse[]>>(
    '/meets/today')

export const getRecentSchedules = (
  days = 5) =>
  api.get<any, ApiResponse<Booking[]>>(
    '/meets/recent', 
    { params: { days } })

export const cancelSchedule = (
  id: number, employeeId: number) =>
  api.delete<any, ApiResponse<void>>(
    `/meets/${id}`,
    { params: { employeeId } })
