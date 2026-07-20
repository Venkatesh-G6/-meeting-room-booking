import { describe, test, expect } from 'vitest'
import dayjs from 'dayjs'
import {
  roomSchema,
  bookingSchema,
  availabilitySchema,
} from '../../utils/validationSchemas'

describe('roomSchema', () => {
  test('validates correctly', () => {
    const valid = {
      roomName: 'Meeting Room A',
      roomType: 'MEETING' as const,
      capacity: 10,
    }
    const result = roomSchema.safeParse(valid)
    expect(result.success).toBe(true)
  })

  test('empty roomName fails', () => {
    const invalid = {
      roomName: '',
      roomType: 'MEETING' as const,
      capacity: 10,
    }
    const result = roomSchema.safeParse(invalid)
    expect(result.success).toBe(false)
  })

  test('capacity 0 fails', () => {
    const invalid = {
      roomName: 'Test Room',
      roomType: 'MEETING' as const,
      capacity: 0,
    }
    const result = roomSchema.safeParse(invalid)
    expect(result.success).toBe(false)
  })

  test('invalid roomType fails', () => {
    const invalid = {
      roomName: 'Test Room',
      roomType: 'INVALID' as const,
      capacity: 10,
    }
    const result = roomSchema.safeParse(invalid)
    expect(result.success).toBe(false)
  })
})

describe('bookingSchema', () => {
  test('validates correctly', () => {
    const valid = {
      roomId: 1,
      title: 'Team Meeting',
      attendeeCount: 5,
      bookedBy: 'admin@company.com',
      startTime: '2025-07-17T10:00:00',
      endTime: '2025-07-17T11:00:00',
    }
    const result = bookingSchema.safeParse(valid)
    expect(result.success).toBe(true)
  })

  test('end before start fails with message', () => {
    const invalid = {
      roomId: 1,
      title: 'Team Meeting',
      attendeeCount: 5,
      bookedBy: 'admin@company.com',
      startTime: '2025-07-17T11:00:00',
      endTime: '2025-07-17T10:00:00',
    }
    const result = bookingSchema.safeParse(invalid)
    expect(result.success).toBe(false)
    if (!result.success) {
      const issue = result.error.issues.find(i => i.path.includes('endTime'))
      expect(issue?.message).toBe('End time must be after start time')
    }
  })

  test('invalid email fails', () => {
    const invalid = {
      roomId: 1,
      title: 'Team Meeting',
      attendeeCount: 5,
      bookedBy: 'not-an-email',
      startTime: '2025-07-17T10:00:00',
      endTime: '2025-07-17T11:00:00',
    }
    const result = bookingSchema.safeParse(invalid)
    expect(result.success).toBe(false)
  })
})

describe('availabilitySchema', () => {
  test('validates correctly', () => {
    const valid = {
      date: dayjs().add(1, 'day').format('YYYY-MM-DD'),
      startTime: '10:00',
      endTime: '11:00',
      minCapacity: 1,
    }
    const result = availabilitySchema.safeParse(valid)
    expect(result.success).toBe(true)
  })

  test('end before start fails', () => {
    const invalid = {
      date: dayjs().add(1, 'day').format('YYYY-MM-DD'),
      startTime: '11:00',
      endTime: '10:00',
      minCapacity: 1,
    }
    const result = availabilitySchema.safeParse(invalid)
    expect(result.success).toBe(false)
    if (!result.success) {
      const issue = result.error.issues.find(i => i.path.includes('endTime'))
      expect(issue?.message).toBe('End time must be after start time')
    }
  })
})
