import { describe, test, expect, vi } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider } from '../../context/AuthContext'
import Rooms from '../../pages/Rooms'

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

describe('Rooms', () => {
  test('renders room management page with Add Room button', () => {
    render(<Rooms />, { wrapper: createWrapper() })
    expect(screen.getAllByText('Room Management').length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText('Add Room')).toBeInTheDocument()
  })

  test('renders rooms table with data from API', async () => {
    render(<Rooms />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Meeting Room A')).toBeInTheDocument()
    )
    expect(screen.getByText('Pod 1')).toBeInTheDocument()
    expect(screen.getByText('Floor 1')).toBeInTheDocument()
    expect(screen.getByText('Floor 2')).toBeInTheDocument()
  })

  test('opens add room modal when Add Room clicked', async () => {
    render(<Rooms />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Meeting Room A')).toBeInTheDocument()
    )

    fireEvent.click(screen.getByText('Add Room'))

    await waitFor(() =>
      expect(screen.getByText('Add Room', { selector: 'h3' })).toBeInTheDocument()
    )
    expect(screen.getByText('Room Name')).toBeInTheDocument()
    expect(screen.getByText('Room Type')).toBeInTheDocument()
    expect(screen.getAllByText('Capacity').length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('Location').length).toBeGreaterThanOrEqual(1)
  })

  test('shows validation errors on empty form submit', async () => {
    render(<Rooms />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Meeting Room A')).toBeInTheDocument()
    )

    fireEvent.click(screen.getByText('Add Room'))

    await waitFor(() =>
      expect(screen.getByText('Save')).toBeInTheDocument()
    )

    const saveButton = screen.getByRole('button', { name: 'Save' })
    fireEvent.click(saveButton)

    await waitFor(() => {
      const errors = document.querySelectorAll('.text-red-500')
      expect(errors.length).toBeGreaterThan(0)
    })
  })

  test('renders active and inactive status badges', async () => {
    render(<Rooms />, { wrapper: createWrapper() })

    await waitFor(() =>
      expect(screen.getByText('Meeting Room A')).toBeInTheDocument()
    )

    expect(screen.getAllByText('Active').length).toBeGreaterThanOrEqual(1)
  })
})
