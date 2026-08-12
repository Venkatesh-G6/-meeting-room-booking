import { useState, useEffect, useCallback, useMemo } from 'react'
import {
  Building2,
  CalendarPlus,
  CalendarDays,
  Loader2,
  X,
  CheckCircle2,
  XCircle,
  Clock,
  Ban,
  ListChecks,
  Lightbulb,
  RefreshCw,
  DoorOpen,
} from 'lucide-react'
import dayjs from 'dayjs'
import toast from 'react-hot-toast'
import { formatDate, formatTime } from '../../utils/dateUtils'
import {
  getEmployees,
  getRooms,
  checkAvailability,
  createBooking,
  getTodayBookings,
  getRecentBookings,
  cancelBooking,
} from '../../api/bookingApi'
import type {
  Employee,
  Room,
  AvailabilityResponse,
  Booking,
  TodayBookingsResponse,
} from '../../types'

type View = 'none' | 'booking' | 'today'

const INPUT_CLASS =
  'border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 w-full'
const READONLY_CLASS =
  'bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 text-sm text-gray-700 w-full'
const PRIMARY_BTN =
  'flex items-center gap-2 px-5 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed'
const OUTLINE_BTN =
  'flex items-center gap-2 px-5 py-2.5 border border-blue-600 text-blue-600 rounded-lg text-sm font-medium hover:bg-blue-50 transition-colors'

function isBookingCurrent(b: Booking, now: dayjs.Dayjs): boolean {
  return dayjs(b.startTime).isBefore(now.add(1, 'second')) && dayjs(b.endTime).isAfter(now)
}

function relativeDayLabel(date: dayjs.Dayjs): string {
  const diff = dayjs().startOf('day').diff(date, 'day')
  if (diff === 0) return 'Today'
  if (diff === 1) return 'Yesterday'
  return `${diff} days ago`
}

export default function BookingPage() {
  const [employees, setEmployees] = useState<Employee[]>([])
  const [rooms, setRooms] = useState<Room[]>([])
  const [selectedRoomId, setSelectedRoomId] = useState<number | null>(null)
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [view, setView] = useState<View>('none')
  const [now, setNow] = useState(dayjs())

  useEffect(() => {
    document.title = 'Room Booking — Technoidentity'
  }, [])

  useEffect(() => {
    const timer = setInterval(() => setNow(dayjs()), 60000)
    return () => clearInterval(timer)
  }, [])

  useEffect(() => {
    async function fetchData() {
      try {
        const [empRes, roomRes] = await Promise.all([
          getEmployees(),
          getRooms(),
        ])
        setEmployees(empRes.data)
        setRooms(roomRes.data)
      } catch (err) {
        console.error('Failed to fetch data:', err)
      } finally {
        setIsLoading(false)
      }
    }
    fetchData()
  }, [])

  const selectedRoom = useMemo(
    () => rooms.find((r) => r.id === selectedRoomId),
    [rooms, selectedRoomId]
  )
  const selectedEmployee = useMemo(
    () => employees.find((e) => e.id === selectedEmployeeId),
    [employees, selectedEmployeeId]
  )

  return (
    <div className="min-h-screen bg-white">
      {/* Section 1 — Header */}
      <header className="bg-blue-700 text-white h-20 flex items-center justify-between px-8">
        <div className="flex items-center gap-3">
          <Building2 className="w-8 h-8" />
          <div>
            <h1 className="text-xl font-bold leading-tight">Room Booking System</h1>
            <p className="text-xs text-blue-200">Technoidentity</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-sm font-medium">
            {now.format('dddd, MMMM D, YYYY')}
          </p>
          <p className="text-xs text-blue-200 tabular-nums">{now.format('hh:mm:ss A')}</p>
        </div>
      </header>

      {/* Section 2 — Main Content */}
      <main className="max-w-[1200px] mx-auto pt-8 px-6">
        {isLoading ? (
          <div className="flex items-center justify-center h-64">
            <Loader2 className="w-8 h-8 text-blue-600 animate-spin" />
          </div>
        ) : (
          <div className="space-y-6">
            {/* Card 1 — Book a Room */}
            <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
              <div className="flex items-center gap-2 mb-5">
                <CalendarPlus className="w-6 h-6 text-blue-600" />
                <h2 className="text-lg font-bold text-gray-800">Book a Room</h2>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-5">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Select Room
                  </label>
                  <select
                    value={selectedRoomId ?? ''}
                    onChange={(e) =>
                      setSelectedRoomId(e.target.value ? Number(e.target.value) : null)
                    }
                    disabled={isLoading}
                    className={`${INPUT_CLASS} disabled:opacity-50`}
                  >
                    {isLoading ? (
                      <option>Loading...</option>
                    ) : rooms.length === 0 ? (
                      <option>No rooms available</option>
                    ) : (
                      <>
                        <option value="">Choose a room...</option>
                        {rooms.map((room) => (
                          <option key={room.id} value={room.id}>
                            {room.roomName}
                          </option>
                        ))}
                      </>
                    )}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Select Employee
                  </label>
                  <select
                    value={selectedEmployeeId ?? ''}
                    onChange={(e) =>
                      setSelectedEmployeeId(
                        e.target.value ? Number(e.target.value) : null
                      )
                    }
                    disabled={isLoading}
                    className={`${INPUT_CLASS} disabled:opacity-50`}
                  >
                    {isLoading ? (
                      <option>Loading...</option>
                    ) : employees.length === 0 ? (
                      <option>No employees found</option>
                    ) : (
                      <>
                        <option value="">Who is booking...</option>
                        {employees.map((emp) => (
                          <option key={emp.id} value={emp.id}>
                            {emp.name} — {emp.department}
                          </option>
                        ))}
                      </>
                    )}
                  </select>
                </div>
              </div>

              <div className="flex gap-3">
                <button
                  onClick={() => setView('booking')}
                  disabled={!selectedRoomId || !selectedEmployeeId}
                  className={PRIMARY_BTN}
                >
                  <CalendarPlus className="w-4 h-4" />
                  Book Selected Room
                </button>
                <button
                  onClick={() => setView('today')}
                  className={OUTLINE_BTN}
                >
                  <CalendarDays className="w-4 h-4" />
                  Today's Bookings
                </button>
              </div>
            </div>

            {/* Card 2 — Results Area */}
            {view !== 'none' && (
              <div className="bg-white rounded-lg shadow-md border border-gray-100 p-6">
                {view === 'booking' && selectedRoom && selectedEmployee && (
                  <BookingSection
                    room={selectedRoom}
                    employee={selectedEmployee}
                    onClose={() => setView('none')}
                    onBooked={() => setView('today')}
                  />
                )}
                {view === 'today' && (
                  <TodayBookingsView
                    onClose={() => setView('none')}
                    selectedEmployeeId={selectedEmployeeId}
                    now={now}
                  />
                )}
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  )
}

function BookingSection({
  room,
  employee,
  onClose,
  onBooked,
}: {
  room: Room
  employee: Employee
  onClose: () => void
  onBooked: () => void
}) {
  const [title, setTitle] = useState('')
  const [date, setDate] = useState('')
  const [startTime, setStartTime] = useState('')
  const [endTime, setEndTime] = useState('')
  const [availability, setAvailability] = useState<AvailabilityResponse | null>(null)
  const [checking, setChecking] = useState(false)
  const [bookingLoading, setBookingLoading] = useState(false)
  const [error, setError] = useState('')
  const today = dayjs().format('YYYY-MM-DD')

  const handleCheck = useCallback(async () => {
    setChecking(true)
    setError('')
    setAvailability(null)
    try {
      const res = await checkAvailability(room.id, date, startTime, endTime)
      setAvailability(res.data)
    } catch (err: any) {
      setError(err)
    } finally {
      setChecking(false)
    }
  }, [room.id, date, startTime, endTime])

  const handleBook = useCallback(async () => {
    setBookingLoading(true)
    setError('')
    try {
      await createBooking({
        roomId: room.id,
        employeeId: employee.id,
        title,
        date,
        startTime,
        endTime,
      })
      toast.success('Room booked successfully!')
      setTitle('')
      setDate('')
      setStartTime('')
      setEndTime('')
      setAvailability(null)
      onBooked()
    } catch (err: any) {
      setError(err)
      toast.error(err)
    } finally {
      setBookingLoading(false)
    }
  }, [room.id, employee.id, title, date, startTime, endTime, onBooked])

  const handleBookSuggested = useCallback(async () => {
    if (!availability?.suggestedStartTime || !availability?.suggestedEndTime) return
    const newStart = availability.suggestedStartTime
    const newEnd = availability.suggestedEndTime
    setStartTime(newStart)
    setEndTime(newEnd)
    setChecking(true)
    setError('')
    setAvailability(null)
    try {
      const res = await checkAvailability(room.id, date, newStart, newEnd)
      setAvailability(res.data)
    } catch (err: any) {
      setError(err)
    } finally {
      setChecking(false)
    }
  }, [availability, room.id, date])

  const isEndBeforeStart = useMemo(
    () => !!startTime && !!endTime && endTime <= startTime,
    [startTime, endTime]
  )

  const canCheck = !!date && !!startTime && !!endTime && !isEndBeforeStart
  const canBook = canCheck && !!title && availability?.available === true

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <div className="flex items-center gap-2">
          <CalendarPlus className="w-6 h-6 text-blue-600" />
          <h3 className="text-lg font-bold text-gray-800">
            Book {room.roomName}
          </h3>
        </div>
        <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
          <X className="w-5 h-5" />
        </button>
      </div>

      <div className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Room</label>
            <input
              type="text"
              readOnly
              value={`${room.roomName} (Capacity: ${room.capacity})`}
              className={READONLY_CLASS}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Employee</label>
            <input
              type="text"
              readOnly
              value={`${employee.name} (${employee.email})`}
              className={READONLY_CLASS}
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Meeting Title <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="e.g. Sprint Planning"
            className={INPUT_CLASS}
          />
        </div>

        <div className="grid grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Date <span className="text-red-500">*</span>
            </label>
            <input
              type="date"
              min={today}
              value={date}
              onChange={(e) => {
                setDate(e.target.value)
                setAvailability(null)
              }}
              className={INPUT_CLASS}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Start Time <span className="text-red-500">*</span>
            </label>
            <input
              type="time"
              value={startTime}
              onChange={(e) => {
                setStartTime(e.target.value)
                setAvailability(null)
              }}
              className={INPUT_CLASS}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              End Time <span className="text-red-500">*</span>
            </label>
            <input
              type="time"
              value={endTime}
              onChange={(e) => {
                setEndTime(e.target.value)
                setAvailability(null)
              }}
              className={`${INPUT_CLASS} ${
                isEndBeforeStart ? 'border-red-300 focus:ring-red-200' : ''
              }`}
            />
            {isEndBeforeStart && (
              <p className="text-xs text-red-600 mt-1">
                End time must be after start time
              </p>
            )}
          </div>
        </div>

        <button
          onClick={handleCheck}
          disabled={!canCheck || checking}
          className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {checking ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <ListChecks className="w-4 h-4" />
          )}
          Check Availability
        </button>

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {availability && availability.available && (
          <div className="space-y-3">
            <div className="bg-green-50 border border-green-200 rounded-lg p-4 flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-green-600" />
              <span className="text-sm font-medium text-gray-800">
                Room is available for the selected time slot
              </span>
            </div>
            <button
              onClick={handleBook}
              disabled={!canBook || bookingLoading}
              className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 transition-colors disabled:opacity-50"
            >
              {bookingLoading ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <CheckCircle2 className="w-4 h-4" />
              )}
              Confirm Booking
            </button>
          </div>
        )}

        {availability && !availability.available && (
          <div className="space-y-3">
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 space-y-1">
              <div className="flex items-center gap-2">
                <XCircle className="w-5 h-5 text-red-600" />
                <span className="text-sm font-medium text-gray-800">
                  {availability.message}
                </span>
              </div>
              {availability.conflictingBooking && (
                <div className="ml-7 text-sm text-gray-600 space-y-0.5">
                  <p>
                    Booked by:{' '}
                    <span className="font-medium">
                      {availability.conflictingBooking.employeeName}
                    </span>
                  </p>
                  <p>
                    From:{' '}
                    <span className="font-medium">
                      {dayjs(availability.conflictingBooking.startTime).format('hh:mm A')}
                    </span>{' '}
                    To:{' '}
                    <span className="font-medium">
                      {dayjs(availability.conflictingBooking.endTime).format('hh:mm A')}
                    </span>
                  </p>
                </div>
              )}
            </div>

            {availability.suggestedStartTime && availability.suggestedEndTime && (
              <div className="space-y-3">
                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 flex items-center gap-2">
                  <Lightbulb className="w-5 h-5 text-yellow-600" />
                  <span className="text-sm font-medium text-gray-800">
                    Next available slot: {availability.suggestedStartTime} to{' '}
                    {availability.suggestedEndTime}
                  </span>
                </div>
                <button
                  onClick={handleBookSuggested}
                  className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-yellow-500 text-white rounded-lg text-sm font-medium hover:bg-yellow-600 transition-colors"
                >
                  <Clock className="w-4 h-4" />
                  Book Suggested Slot
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

function TodayBookingsView({
  onClose,
  selectedEmployeeId,
  now,
}: {
  onClose: () => void
  selectedEmployeeId: number | null
  now: dayjs.Dayjs
}) {
  const [todayData, setTodayData] = useState<TodayBookingsResponse[]>([])
  const [recentData, setRecentData] = useState<Booking[]>([])
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [showRecent, setShowRecent] = useState(false)
  const [confirming, setConfirming] = useState<Booking | null>(null)

  const fetchAll = useCallback(async () => {
    setRefreshing(true)
    try {
      const [todayRes, recentRes] = await Promise.all([
        getTodayBookings(),
        getRecentBookings(5),
      ])
      setTodayData(todayRes.data)
      setRecentData(recentRes.data)
    } catch (err) {
      console.error('Failed to fetch bookings:', err)
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }, [])

  useEffect(() => {
    fetchAll()
  }, [fetchAll])

  const confirmCancel = useCallback(async () => {
    if (!confirming) return
    try {
      await cancelBooking(confirming.id, confirming.employeeId)
      toast.success('Booking cancelled successfully')
      await fetchAll()
    } catch (err: any) {
      toast.error(err)
    } finally {
      setConfirming(null)
    }
  }, [confirming, fetchAll])

  if (loading && !refreshing) {
    return (
      <div className="flex items-center justify-center h-32">
        <Loader2 className="w-6 h-6 text-blue-600 animate-spin" />
      </div>
    )
  }

  const titleDate = showRecent
    ? 'Last 5 Days'
    : `Today's Bookings — ${formatDate(dayjs().toISOString())}`

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <CalendarDays className="w-6 h-6 text-blue-600" />
          <h3 className="text-lg font-bold text-gray-800">{titleDate}</h3>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowRecent(false)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              !showRecent
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            Today
          </button>
          <button
            onClick={() => setShowRecent(true)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              showRecent
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            Last 5 Days
          </button>
          <button
            onClick={fetchAll}
            disabled={refreshing}
            className="flex items-center gap-1 px-3 py-1.5 bg-gray-100 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-200 transition-colors disabled:opacity-50"
          >
            <RefreshCw
              className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`}
            />
            Refresh
          </button>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 ml-2">
            <X className="w-5 h-5" />
          </button>
        </div>
      </div>

      {!showRecent ? (
        <div className="space-y-4">
          {todayData.length === 0 ? (
            <p className="text-sm text-gray-400 text-center py-8">
              No bookings for today yet
            </p>
          ) : (
            todayData.map((room) => {
              const current = room.bookings.find((b) => isBookingCurrent(b, now))
              const statusColor = current
                ? 'bg-orange-500'
                : room.fullyAvailable
                ? 'bg-green-500'
                : 'bg-gray-400'

              return (
                <div
                  key={room.roomId}
                  className="border border-gray-200 rounded-xl p-5 bg-white shadow-sm"
                >
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className="relative">
                        <div className="w-10 h-10 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center">
                          <DoorOpen className="w-5 h-5" />
                        </div>
                        <span
                          className={`absolute -top-1 -right-1 w-3 h-3 rounded-full border-2 border-white ${statusColor}`}
                          title={
                            current
                              ? 'In use'
                              : room.fullyAvailable
                              ? 'Available'
                              : 'Has bookings'
                          }
                        />
                      </div>
                      <div>
                        <p className="text-base font-bold text-gray-800">
                          {room.roomName}
                        </p>
                        <p className="text-xs text-gray-500">{room.location}</p>
                      </div>
                    </div>
                    {room.fullyAvailable ? (
                      <span className="text-sm font-medium text-green-600 flex items-center gap-1">
                        <CheckCircle2 className="w-4 h-4" />
                        Available all day
                      </span>
                    ) : (
                      <span className="text-xs font-medium px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-700">
                        {room.bookings.length} booking
                        {room.bookings.length > 1 ? 's' : ''}
                      </span>
                    )}
                  </div>

                  {room.bookings.length > 0 ? (
                    <div className="space-y-3 pl-1">
                      {room.bookings.map((b) => {
                        const isCurrent = isBookingCurrent(b, now)
                        return (
                          <div
                            key={b.id}
                            className={`border-l-4 pl-4 py-1 ${
                              isCurrent
                                ? 'border-yellow-400 bg-yellow-50 rounded-r-lg'
                                : 'border-blue-500'
                            }`}
                          >
                            <p className="text-sm font-semibold text-gray-800">
                              {formatTime(b.startTime)} - {formatTime(b.endTime)} | {b.title}
                            </p>
                            <p className="text-sm text-gray-600 flex items-center gap-1 mt-1">
                              <span className="w-2 h-2 rounded-full bg-gray-400" />
                              {b.employeeName}
                            </p>
                            {b.employeeId === selectedEmployeeId &&
                              b.status === 'CONFIRMED' && (
                                <button
                                  onClick={() => setConfirming(b)}
                                  className="mt-2 flex items-center gap-1 text-red-600 hover:text-red-700 text-sm font-medium transition-colors"
                                >
                                  <Ban className="w-4 h-4" />
                                  Cancel
                                </button>
                              )}
                          </div>
                        )
                      })}
                      <p className="text-sm font-medium text-green-600 flex items-center gap-1 pt-2">
                        <CheckCircle2 className="w-4 h-4" />
                        No more bookings today
                      </p>
                    </div>
                  ) : (
                    <p className="text-sm font-medium text-green-600 flex items-center gap-1 pl-1">
                      <CheckCircle2 className="w-4 h-4" />
                      Available all day
                    </p>
                  )}
                </div>
              )
            })
          )}
        </div>
      ) : (
        <RecentDaysView
          recentData={recentData}
          selectedEmployeeId={selectedEmployeeId}
          onCancel={setConfirming}
        />
      )}

      {confirming && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-lg p-6 w-full max-w-sm mx-4">
            <h4 className="text-lg font-bold text-gray-800 mb-2">
              Cancel this booking?
            </h4>
            <p className="text-sm text-gray-600 mb-6">
              {confirming.title} in {confirming.roomName} at{' '}
              {formatTime(confirming.startTime)}
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setConfirming(null)}
                className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
              >
                No Keep It
              </button>
              <button
                onClick={confirmCancel}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-medium hover:bg-red-700 transition-colors"
              >
                Yes Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function RecentDaysView({
  recentData,
  selectedEmployeeId,
  onCancel,
}: {
  recentData: Booking[]
  selectedEmployeeId: number | null
  onCancel: (booking: Booking) => void
}) {
  const grouped = new Map<string, Booking[]>()
  recentData.forEach((b) => {
    const key = dayjs(b.startTime).startOf('day').toISOString()
    if (!grouped.has(key)) grouped.set(key, [])
    grouped.get(key)?.push(b)
  })

  const sortedKeys = Array.from(grouped.keys()).sort(
    (a, b) => dayjs(b).valueOf() - dayjs(a).valueOf()
  )

  if (sortedKeys.length === 0) {
    return (
      <p className="text-sm text-gray-400 text-center py-8">
        No recent bookings found
      </p>
    )
  }

  return (
    <div className="space-y-6">
      {sortedKeys.map((key) => {
        const date = dayjs(key)
        const heading = relativeDayLabel(date)
        const bookings = grouped.get(key) || []

        return (
          <div key={key}>
            <h4 className="text-lg font-bold text-gray-800 mb-3">
              {heading} — {formatDate(date.toISOString())}
            </h4>
            <div className="overflow-hidden border border-gray-200 rounded-xl">
              <table className="w-full">
                <thead>
                  <tr className="bg-gray-50 text-left">
                    <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Room
                    </th>
                    <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Employee
                    </th>
                    <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Title
                    </th>
                    <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Time
                    </th>
                    <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Status
                    </th>
                    <th className="px-4 py-3 text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Action
                    </th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200 bg-white">
                  {bookings.map((b) => (
                    <tr key={b.id} className="hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm font-medium text-gray-800">
                        {b.roomName}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {b.employeeName}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {b.title}
                      </td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {formatTime(b.startTime)} - {formatTime(b.endTime)}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${
                            b.status === 'CONFIRMED'
                              ? 'bg-green-100 text-green-700'
                              : 'bg-red-100 text-red-700'
                          }`}
                        >
                          {b.status}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {b.employeeId === selectedEmployeeId &&
                          b.status === 'CONFIRMED' && (
                            <button
                              onClick={() => onCancel(b)}
                              className="flex items-center gap-1 text-red-600 hover:text-red-700 text-sm font-medium transition-colors"
                            >
                              <Ban className="w-4 h-4" />
                              Cancel
                            </button>
                          )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )
      })}
    </div>
  )
}
