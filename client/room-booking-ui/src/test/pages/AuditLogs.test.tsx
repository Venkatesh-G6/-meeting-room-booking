import { describe, test, expect } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider } from '../../context/AuthContext'
import AuditLogs from '../../pages/AuditLogs'

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

describe('AuditLogs', () => {
  test('renders audit logs page with title', () => {
    render(<AuditLogs />, { wrapper: createWrapper() })
    expect(screen.getAllByText('Audit Logs').length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText('Complete action history')).toBeInTheDocument()
  })

  test('renders audit log table with data from API', async () => {
    render(<AuditLogs />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('ROOM_CREATED')).toBeInTheDocument()
    )
    expect(screen.getByText('BOOKING_CREATED')).toBeInTheDocument()
    expect(screen.getByText('BOOKING_CANCELLED')).toBeInTheDocument()
  })

  test('renders filter controls', () => {
    render(<AuditLogs />, { wrapper: createWrapper() })

    expect(screen.getByPlaceholderText('Search by actor email...')).toBeInTheDocument()
    expect(screen.getByText('Clear Filters')).toBeInTheDocument()
  })

  test('filters by action type', async () => {
    render(<AuditLogs />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('ROOM_CREATED')).toBeInTheDocument()
    )

    const selects = document.querySelectorAll('select')
    const actionSelect = selects[selects.length - 1]
    fireEvent.change(actionSelect, { target: { value: 'BOOKING_CREATED' } })

    await waitFor(() => {
      const spans = document.querySelectorAll('tbody span')
      const actions = Array.from(spans).map(s => s.textContent)
      expect(actions).not.toContain('ROOM_CREATED')
      expect(actions).toContain('BOOKING_CREATED')
    })
  })

  test('opens detail modal when View clicked', async () => {
    render(<AuditLogs />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.findAllByText('View')).resolves.toHaveLength(3)
    )

    const viewButtons = await screen.findAllByText('View')
    fireEvent.click(viewButtons[0])

    await waitFor(() =>
      expect(screen.getByText('Audit Log Details')).toBeInTheDocument()
    )
    expect(screen.getAllByText('Action').length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('Actor').length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('Entity').length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('Timestamp').length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText('Metadata')).toBeInTheDocument()
  })

  test('clear filters resets the list', async () => {
    render(<AuditLogs />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('ROOM_CREATED')).toBeInTheDocument()
    )

    const selects = document.querySelectorAll('select')
    const actionSelect = selects[selects.length - 1]
    fireEvent.change(actionSelect, { target: { value: 'BOOKING_CREATED' } })

    await waitFor(() => {
      const spans = document.querySelectorAll('tbody span')
      const actions = Array.from(spans).map(s => s.textContent)
      expect(actions).not.toContain('ROOM_CREATED')
    })

    fireEvent.click(screen.getByText('Clear Filters'))

    await waitFor(() => {
      const spans = document.querySelectorAll('tbody span')
      const actions = Array.from(spans).map(s => s.textContent)
      expect(actions).toContain('ROOM_CREATED')
    })
  })
})
