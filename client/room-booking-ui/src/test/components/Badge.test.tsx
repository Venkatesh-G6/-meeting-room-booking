import { describe, test, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Badge from '../../components/common/Badge'

describe('Badge', () => {
  test('renders CONFIRMED badge in green', () => {
    render(<Badge status="CONFIRMED" />)
    const badge = screen.getByText('CONFIRMED')
    expect(badge).toBeInTheDocument()
    expect(badge).toHaveClass('bg-green-100')
  })

  test('renders CANCELLED badge in red', () => {
    render(<Badge status="CANCELLED" />)
    expect(screen.getByText('CANCELLED')).toHaveClass('bg-red-100')
  })

  test('renders MEETING badge in blue', () => {
    render(<Badge status="MEETING" />)
    expect(screen.getByText('MEETING')).toHaveClass('bg-blue-100')
  })

  test('renders active true as green', () => {
    render(<Badge status={true} />)
    expect(screen.getByText('Active')).toBeInTheDocument()
    expect(screen.getByText('Active')).toHaveClass('bg-green-100')
  })

  test('renders active false as gray', () => {
    render(<Badge status={false} />)
    expect(screen.getByText('Inactive')).toBeInTheDocument()
    expect(screen.getByText('Inactive')).toHaveClass('bg-gray-100')
  })
})
