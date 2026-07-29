import { describe, test, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { ErrorBoundary } from '../../components/common/ErrorBoundary'

function ThrowOnRender({ message }: { message: string }) {
  throw new Error(message)
}

function SafeChild() {
  return <div>Safe Child Content</div>
}

describe('ErrorBoundary', () => {
  test('renders children when no error', () => {
    render(
      <ErrorBoundary>
        <SafeChild />
      </ErrorBoundary>
    )
    expect(screen.getByText('Safe Child Content')).toBeInTheDocument()
  })

  test('renders error UI when child throws', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {})

    render(
      <ErrorBoundary>
        <ThrowOnRender message="Test error occurred" />
      </ErrorBoundary>
    )

    expect(screen.getByText('Oops! Something went wrong')).toBeInTheDocument()
    expect(screen.getByText('Test error occurred')).toBeInTheDocument()

    vi.restoreAllMocks()
  })

  test('renders Reload Page and Go to Dashboard buttons in error state', () => {
    vi.spyOn(console, 'error').mockImplementation(() => {})

    render(
      <ErrorBoundary>
        <ThrowOnRender message="Something broke" />
      </ErrorBoundary>
    )

    expect(screen.getByText('Reload Page')).toBeInTheDocument()
    expect(screen.getByText('Go to Dashboard')).toBeInTheDocument()

    vi.restoreAllMocks()
  })
})
