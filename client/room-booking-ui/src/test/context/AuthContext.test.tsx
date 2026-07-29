import { describe, test, expect, beforeEach, afterEach, vi } from 'vitest'
import { render, renderHook, act, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { AuthProvider, type AuthUser } from '../../context/AuthContext'
import { useAuth } from '../../context/auth-utils'

const mockUser: AuthUser = {
  email: 'admin@company.com',
  displayName: 'Admin User',
  role: 'ADMIN',
  token: 'test-token',
}

const wrapper = ({ children }: { children: React.ReactNode }) => (
  <MemoryRouter>
    <AuthProvider>{children}</AuthProvider>
  </MemoryRouter>
)

describe('AuthContext', () => {
  beforeEach(() => {
    sessionStorage.clear()
  })

  afterEach(() => {
    sessionStorage.clear()
  })

  test('starts unauthenticated with isLoading false after mount', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper })

    await vi.waitFor(() => {
      expect(result.current.isLoading).toBe(false)
    })
    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.user).toBeNull()
  })

  test('login sets user and persists to sessionStorage', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })

    act(() => {
      result.current.login(mockUser)
    })

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.user).toEqual(mockUser)
    expect(result.current.isAdmin).toBe(true)

    const stored = sessionStorage.getItem('roombooking_user')
    expect(stored).not.toBeNull()
    expect(JSON.parse(stored!).email).toBe('admin@company.com')
  })

  test('logout clears user and sessionStorage', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })

    act(() => {
      result.current.login(mockUser)
    })
    expect(result.current.isAuthenticated).toBe(true)

    act(() => {
      result.current.logout()
    })

    expect(result.current.isAuthenticated).toBe(false)
    expect(result.current.user).toBeNull()
    expect(sessionStorage.getItem('roombooking_user')).toBeNull()
  })

  test('restores user from sessionStorage on mount', async () => {
    sessionStorage.setItem('roombooking_user', JSON.stringify(mockUser))

    const { result } = renderHook(() => useAuth(), { wrapper })

    await vi.waitFor(() => {
      expect(result.current.isAuthenticated).toBe(true)
    })
    expect(result.current.user?.email).toBe('admin@company.com')
    expect(result.current.isAdmin).toBe(true)
  })

  test('isAdmin is false for EMPLOYEE role', () => {
    const { result } = renderHook(() => useAuth(), { wrapper })

    act(() => {
      result.current.login({ ...mockUser, role: 'EMPLOYEE' })
    })

    expect(result.current.isAuthenticated).toBe(true)
    expect(result.current.isAdmin).toBe(false)
  })

  test('useAuth throws when used outside AuthProvider', () => {
    expect(() => renderHook(() => useAuth())).toThrow(
      'useAuth must be used within AuthProvider'
    )
  })
})
