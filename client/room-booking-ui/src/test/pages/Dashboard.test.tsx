import { describe, test, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider } from '../../context/AuthContext'
import Dashboard from '../../pages/Dashboard'

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  })
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter>{children}</MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>
  )
}

describe('Dashboard', () => {
  test('renders stat cards', async () => {
    render(<Dashboard />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Total Rooms')).toBeInTheDocument()
    )
    expect(screen.getByText('Active Rooms')).toBeInTheDocument()
  })

  test('shows correct room count from API', async () => {
    render(<Dashboard />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getAllByText('2').length).toBeGreaterThanOrEqual(1)
    )
  })

  test('shows recent bookings table', async () => {
    render(<Dashboard />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Recent Bookings')).toBeInTheDocument()
    )
  })
})
