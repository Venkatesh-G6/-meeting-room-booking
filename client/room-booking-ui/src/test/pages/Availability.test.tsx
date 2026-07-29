import { describe, test, expect } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider } from '../../context/AuthContext'
import Availability from '../../pages/Availability'
import dayjs from 'dayjs'

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

describe('Availability', () => {
  test('renders search form', () => {
    render(<Availability />, { wrapper: createWrapper() })

    expect(screen.getByText('Date')).toBeInTheDocument()
    expect(screen.getByText('Start Time')).toBeInTheDocument()
    expect(screen.getByText('End Time')).toBeInTheDocument()
  })

  test('shows validation error for empty date', async () => {
    render(<Availability />, { wrapper: createWrapper() })

    const submitButton = screen.getByText('Check Availability')
    fireEvent.click(submitButton)

    await waitFor(() => {
      const errorMessages = document.querySelectorAll('.text-red-500')
      expect(errorMessages.length).toBeGreaterThan(0)
    })
  })

  test('shows results after search', async () => {
    render(<Availability />, { wrapper: createWrapper() })

    const tomorrow = dayjs().add(1, 'day').format('YYYY-MM-DD')
    const dateInput = document.querySelector('input[type="date"]') as HTMLInputElement
    const startTimeInput = document.querySelectorAll('input[type="time"]')[0] as HTMLInputElement
    const endTimeInput = document.querySelectorAll('input[type="time"]')[1] as HTMLInputElement

    fireEvent.change(dateInput, { target: { value: tomorrow } })
    fireEvent.change(startTimeInput, { target: { value: '10:00' } })
    fireEvent.change(endTimeInput, { target: { value: '11:00' } })

    const submitButton = screen.getByText('Check Availability')
    fireEvent.click(submitButton)

    await waitFor(() => {
      expect(screen.getByText(/available/i)).toBeInTheDocument()
    })
  })

  test('book now button opens modal', async () => {
    render(<Availability />, { wrapper: createWrapper() })

    const tomorrow = dayjs().add(1, 'day').format('YYYY-MM-DD')
    const dateInput = document.querySelector('input[type="date"]') as HTMLInputElement
    const startTimeInput = document.querySelectorAll('input[type="time"]')[0] as HTMLInputElement
    const endTimeInput = document.querySelectorAll('input[type="time"]')[1] as HTMLInputElement

    fireEvent.change(dateInput, { target: { value: tomorrow } })
    fireEvent.change(startTimeInput, { target: { value: '10:00' } })
    fireEvent.change(endTimeInput, { target: { value: '11:00' } })

    fireEvent.click(screen.getByText('Check Availability'))

    await waitFor(() => {
      expect(screen.getByText('Book Now')).toBeInTheDocument()
    })

    fireEvent.click(screen.getByText('Book Now'))

    await waitFor(() => {
      expect(screen.getByText(/Book Meeting Room A/i)).toBeInTheDocument()
    })
  })
})
