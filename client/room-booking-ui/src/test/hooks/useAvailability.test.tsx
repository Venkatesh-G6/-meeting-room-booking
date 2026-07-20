import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import type { ReactNode } from 'react'
import { describe, test, expect } from 'vitest'
import { useAvailability } from '../../hooks'

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

const mockParams = {
  date: '2025-07-17',
  startTime: '10:00',
  endTime: '11:00',
  minCapacity: 1,
}

describe('useAvailability', () => {
  test('disabled by default', () => {
    const { result } = renderHook(
      () => useAvailability(mockParams, false),
      { wrapper: createWrapper() }
    )

    expect(result.current.fetchStatus).toBe('idle')
    expect(result.current.isLoading).toBe(false)
  })

  test('fetches when enabled', async () => {
    const { result } = renderHook(
      () => useAvailability(mockParams, true),
      { wrapper: createWrapper() }
    )

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
  })

  test('returns rooms', async () => {
    const { result } = renderHook(
      () => useAvailability(mockParams, true),
      { wrapper: createWrapper() }
    )

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.availableRooms).toHaveLength(1)
    expect(result.current.data?.totalAvailable).toBe(1)
  })
})
