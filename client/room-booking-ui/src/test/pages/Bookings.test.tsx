import { describe, test, expect, vi } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider } from '../../context/AuthContext'
import Bookings from '../../pages/Bookings'

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  })
  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <AuthProvider>{children}</AuthProvider>
      </MemoryRouter>
    </QueryClientProvider>
  )
}

describe('Bookings', () => {
  test('renders bookings page with title', () => {
    render(<Bookings />, { wrapper: createWrapper() })
    expect(screen.getByText('All Bookings')).toBeInTheDocument()
  })

  test('renders bookings table with data from API', async () => {
    render(<Bookings />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Team Standup')).toBeInTheDocument()
    )
    expect(screen.getByText('Client Call')).toBeInTheDocument()
    expect(screen.getAllByText('admin@company.com').length).toBeGreaterThanOrEqual(1)
  })

  test('renders search and filter controls', () => {
    render(<Bookings />, { wrapper: createWrapper() })

    expect(screen.getByPlaceholderText('Search by email...')).toBeInTheDocument()
    expect(screen.getByText('Clear Filters')).toBeInTheDocument()
  })

  test('filters bookings by email', async () => {
    render(<Bookings />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Team Standup')).toBeInTheDocument()
    )

    const searchInput = screen.getByPlaceholderText('Search by email...')
    fireEvent.change(searchInput, { target: { value: 'nonexistent' } })

    await waitFor(() =>
      expect(screen.getByText('No bookings found')).toBeInTheDocument()
    )
  })

  test('shows cancel button for confirmed bookings', async () => {
    render(<Bookings />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Team Standup')).toBeInTheDocument()
    )

    const cancelButtons = screen.getAllByText('Cancel')
    expect(cancelButtons.length).toBeGreaterThan(0)
  })

  test('clear filters button resets search', async () => {
    render(<Bookings />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Team Standup')).toBeInTheDocument()
    )

    const searchInput = screen.getByPlaceholderText('Search by email...')
    fireEvent.change(searchInput, { target: { value: 'nonexistent' } })

    await waitFor(() =>
      expect(screen.getByText('No bookings found')).toBeInTheDocument()
    )

    fireEvent.click(screen.getByText('Clear Filters'))

    await waitFor(() =>
      expect(screen.getByText('Team Standup')).toBeInTheDocument()
    )
  })
})
