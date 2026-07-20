import { renderHook, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { http, HttpResponse } from 'msw'
import type { ReactNode } from 'react'
import { describe, test, expect } from 'vitest'
import { useRooms } from '../../hooks'
import { server } from '../mocks/server'

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

describe('useRooms', () => {
  test('fetches rooms successfully', async () => {
    const { result } = renderHook(() => useRooms(0, 10), {
      wrapper: createWrapper(),
    })

    await waitFor(() => expect(result.current.isSuccess).toBe(true))
    expect(result.current.data?.content).toHaveLength(2)
  })

  test('shows loading initially', () => {
    const { result } = renderHook(() => useRooms(0, 10), {
      wrapper: createWrapper(),
    })

    expect(result.current.isLoading).toBe(true)
  })

  test('handles error', async () => {
    server.use(
      http.get('http://localhost:8080/api/v1/rooms', () =>
        HttpResponse.json(
          { success: false, message: 'Server error' },
          { status: 500 }
        )
      )
    )

    const { result } = renderHook(() => useRooms(0, 10), {
      wrapper: createWrapper(),
    })

    await waitFor(() => expect(result.current.isError).toBe(true))
  })
})
