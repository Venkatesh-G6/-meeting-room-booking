import { describe, test, expect } from 'vitest'
import dayjs from 'dayjs'
import {
  formatDate,
  formatTime,
  formatDateTime,
  formatDateForApi,
  formatTimeForApi,
  isToday,
  isPast,
} from '../../utils/dateUtils'

describe('dateUtils', () => {
  test('formatDate returns correct format', () => {
    const result = formatDate('2025-07-01T10:00:00')
    expect(result).toBe('Tue, 01 Jul 2025')
  })

  test('formatTime returns 12hr format', () => {
    const result = formatTime('2025-07-01T10:00:00')
    expect(result).toBe('10:00 AM')
  })

  test('formatTime handles PM correctly', () => {
    const result = formatTime('2025-07-01T14:00:00')
    expect(result).toBe('02:00 PM')
  })

  test('formatDateTime combines date and time', () => {
    const result = formatDateTime('2025-07-01T10:00:00')
    expect(result).toContain('Jul 2025')
    expect(result).toContain('10:00 AM')
  })

  test('formatDateForApi returns YYYY-MM-DD', () => {
    const date = new Date('2025-07-01')
    const result = formatDateForApi(date)
    expect(result).toBe('2025-07-01')
  })

  test('formatTimeForApi appends seconds', () => {
    const result = formatTimeForApi('10:00')
    expect(result).toBe('10:00:00')
  })

  test('isToday returns true for today', () => {
    const today = new Date().toISOString()
    expect(isToday(today)).toBe(true)
  })

  test('isToday returns false for yesterday', () => {
    const yesterday = dayjs().subtract(1, 'day').toISOString()
    expect(isToday(yesterday)).toBe(false)
  })

  test('isPast returns true for past date', () => {
    const past = dayjs().subtract(1, 'day').toISOString()
    expect(isPast(past)).toBe(true)
  })

  test('isPast returns false for future date', () => {
    const future = dayjs().add(1, 'day').toISOString()
    expect(isPast(future)).toBe(false)
  })
})
