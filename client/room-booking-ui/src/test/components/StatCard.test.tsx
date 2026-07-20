import { describe, test, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Users, Calendar, DoorOpen } from 'lucide-react'
import StatCard from '../../components/common/StatCard'

describe('StatCard', () => {
  test('renders title and value', () => {
    render(<StatCard title="Total Rooms" value={10} icon={Users} color="bg-blue-500" />)
    expect(screen.getByText('Total Rooms')).toBeInTheDocument()
    expect(screen.getByText('10')).toBeInTheDocument()
  })

  test('renders icon correctly', () => {
    const { container } = render(
      <StatCard title="Bookings" value={25} icon={Calendar} color="bg-green-500" />
    )
    const svg = container.querySelector('svg')
    expect(svg).toBeInTheDocument()
  })

  test('applies color class', () => {
    const { container } = render(
      <StatCard title="Available" value="5" icon={DoorOpen} color="bg-purple-500" />
    )
    const colorDiv = container.querySelector('.bg-purple-500')
    expect(colorDiv).toBeInTheDocument()
  })
})
