import { describe, test, expect, beforeEach, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import { AuthProvider } from '../../context/AuthContext'
import ProtectedRoute from '../../components/common/ProtectedRoute'
import type { AuthUser } from '../../context/AuthContext'

vi.mock('react-hot-toast', () => ({
  default: Object.assign(vi.fn(), {
    error: vi.fn(),
    success: vi.fn(),
  }),
}))

const mockUser: AuthUser = {
  email: 'admin@company.com',
  displayName: 'Admin User',
  role: 'ADMIN',
  token: 'test-token',
}

const employeeUser: AuthUser = {
  ...mockUser,
  role: 'EMPLOYEE',
}

function ProtectedContent() {
  return <div>Protected Content</div>
}

function LoginPage() {
  return <div>Login Page</div>
}

function DashboardPage() {
  return <div>Dashboard Page</div>
}

const createWrapper = (initialUser: AuthUser | null = null) => {
  if (initialUser) {
    sessionStorage.setItem('roombooking_user', JSON.stringify(initialUser))
  } else {
    sessionStorage.clear()
  }

  return ({ children }: { children: React.ReactNode }) => (
    <MemoryRouter initialEntries={['/protected']}>
      <AuthProvider>
        <Routes>
          <Route path="/protected" element={
            <ProtectedRoute>{children}</ProtectedRoute>
          } />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/" element={<DashboardPage />} />
        </Routes>
      </AuthProvider>
    </MemoryRouter>
  )
}

describe('ProtectedRoute', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  test('redirects to login when unauthenticated', async () => {
    render(<ProtectedContent />, { wrapper: createWrapper(null) })

    await waitFor(() =>
      expect(screen.getByText('Login Page')).toBeInTheDocument()
    )
  })

  test('renders children when authenticated', async () => {
    render(<ProtectedContent />, { wrapper: createWrapper(mockUser) })

    await waitFor(() =>
      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    )
  })

  test('renders children for admin when requireAdmin is true', async () => {
    const wrapper = ({ children }: { children: React.ReactNode }) => {
      sessionStorage.setItem('roombooking_user', JSON.stringify(mockUser))
      return (
        <MemoryRouter initialEntries={['/protected']}>
          <AuthProvider>
            <Routes>
              <Route path="/protected" element={
                <ProtectedRoute requireAdmin>{children}</ProtectedRoute>
              } />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/" element={<DashboardPage />} />
            </Routes>
          </AuthProvider>
        </MemoryRouter>
      )
    }

    render(<ProtectedContent />, { wrapper })

    await waitFor(() =>
      expect(screen.getByText('Protected Content')).toBeInTheDocument()
    )
  })

  test('redirects non-admin to dashboard when requireAdmin is true', async () => {
    const wrapper = ({ children }: { children: React.ReactNode }) => {
      sessionStorage.setItem('roombooking_user', JSON.stringify(employeeUser))
      return (
        <MemoryRouter initialEntries={['/protected']}>
          <AuthProvider>
            <Routes>
              <Route path="/protected" element={
                <ProtectedRoute requireAdmin>{children}</ProtectedRoute>
              } />
              <Route path="/login" element={<LoginPage />} />
              <Route path="/" element={<DashboardPage />} />
            </Routes>
          </AuthProvider>
        </MemoryRouter>
      )
    }

    render(<ProtectedContent />, { wrapper })

    await waitFor(() =>
      expect(screen.getByText('Dashboard Page')).toBeInTheDocument()
    )
  })
})
