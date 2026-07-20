import { describe, test, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider } from '../../context/AuthContext'
import Login from '../../pages/Login'

const createWrapper = () => {
  return ({ children }: { children: ReactNode }) => (
    <AuthProvider>
      <MemoryRouter>{children}</MemoryRouter>
    </AuthProvider>
  )
}

describe('Login', () => {
  test('renders login card', () => {
    render(<Login />, { wrapper: createWrapper() })
    expect(screen.getByText('Room Booking System')).toBeInTheDocument()
  })

  test('renders dev login button in dev mode', () => {
    vi.stubEnv('VITE_ENV', 'dev')
    render(<Login />, { wrapper: createWrapper() })
    expect(screen.getByText('Dev Mode Login')).toBeInTheDocument()
    vi.unstubAllEnvs()
  })

  test('dev login sets user and redirects', async () => {
    vi.stubEnv('VITE_ENV', 'dev')

    sessionStorage.clear()
    render(<Login />, { wrapper: createWrapper() })

    fireEvent.click(screen.getByText('Dev Mode Login'))

    await waitFor(() => {
      const stored = sessionStorage.getItem('roombooking_user')
      expect(stored).not.toBeNull()
      const user = JSON.parse(stored!)
      expect(user.email).toBe('admin@company.com')
      expect(user.role).toBe('ADMIN')
    })

    vi.unstubAllEnvs()
  })
})
