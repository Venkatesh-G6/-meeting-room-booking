import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { describe, test, expect } from 'vitest'
import {
  useBookings,
  useCancelBooking,
  useCreateBooking,
  useMyBookings,
} from '../../hooks'
import type { CreateBookingRequest } from '../../types'

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  })
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      {children}
    </QueryClientProvider>
  )
}

describe('useBookings', () => {
  test('fetches bookings successfully', async () => {
    const { result } = renderHook(() => useBookings(0, 10), {
      wrapper: createWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(2)
  })

  test('useCancelBooking cancels booking', async () => {
    const { result } = renderHook(() => useCancelBooking(), {
      wrapper: createWrapper(),
    })

    await result.current.mutateAsync('1')

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect((result.current.data?.data as unknown as Record<string, unknown>).status).toBe('CANCELLED')
  })

  test('useCreateBooking creates booking', async () => {
    const { result } = renderHook(() => useCreateBooking(), {
      wrapper: createWrapper(),
    })

    const newBooking: CreateBookingRequest = {
      roomId: '1',
      title: 'Test Meeting',
      attendeeCount: 3,
      startTime: '2025-07-17T10:00:00',
      endTime: '2025-07-17T11:00:00',
    }

    await result.current.mutateAsync(newBooking)

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.data.title).toBe('Test Meeting')
    expect(result.current.data?.data.status).toBe('CONFIRMED')
  })

  test('useMyBookings filters by email', async () => {
    const { result } = renderHook(
      () => useMyBookings('admin@company.com'),
      { wrapper: createWrapper() }
    )

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data).toHaveLength(2)
  })
})
