import { describe, test, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import Pagination from '../../components/common/Pagination'

describe('Pagination', () => {
  const defaultProps = {
    totalElements: 50,
    pageSize: 10,
    onPageChange: vi.fn(),
  }

  test('renders correct page info', () => {
    render(
      <Pagination
        currentPage={0}
        totalPages={5}
        {...defaultProps}
      />
    )
    expect(screen.getByText(/Showing/)).toBeInTheDocument()
    expect(screen.getByText(/1 to 10 of 50 results/)).toBeInTheDocument()
  })

  test('prev button disabled on first page', () => {
    render(
      <Pagination
        currentPage={0}
        totalPages={5}
        {...defaultProps}
      />
    )
    const buttons = screen.getAllByRole('button')
    expect(buttons[0]).toBeDisabled()
  })

  test('next button disabled on last page', () => {
    render(
      <Pagination
        currentPage={4}
        totalPages={5}
        {...defaultProps}
      />
    )
    const buttons = screen.getAllByRole('button')
    const nextButton = buttons[buttons.length - 1]
    expect(nextButton).toBeDisabled()
  })

  test('calls onPageChange when clicked', () => {
    const onPageChange = vi.fn()
    render(
      <Pagination
        currentPage={0}
        totalPages={5}
        totalElements={50}
        pageSize={10}
        onPageChange={onPageChange}
      />
    )
    const buttons = screen.getAllByRole('button')
    const nextButton = buttons[buttons.length - 1]
    fireEvent.click(nextButton)
    expect(onPageChange).toHaveBeenCalledWith(1)
  })
})
