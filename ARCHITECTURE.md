# Architecture

## Current Implementation Status

### Backend — Spring Boot 3 / Java 21
Package: com.yourcompany.roombooking

Layers implemented:
✅ entity/     — Room, Booking, AuditLog
✅ repository/ — All with custom queries
✅ service/    — Interfaces + Impl
✅ controller/ — REST endpoints /api/v1/
✅ dto/        — Request + Response + Paged
✅ enums/      — RoomType, BookingStatus,
                 AuditAction, BotCommand
✅ exception/  — Global handler
✅ config/     — Security, CORS, Swagger,
                 JPA, Logging, RateLimit
✅ audit/      — Async audit service
✅ bot/        — TeamsBot, CommandParser,
                 CardBuilder
✅ graph/      — Stub + Real impl ready

### Database — MySQL
Migrations:
✅ V1 — Initial schema
✅ V2 — Performance indexes
✅ V3 — Users table
✅ V4 — Graph event ID

### Frontend — React + Vite + TypeScript
✅ Pages: Dashboard, Rooms, Bookings,
          Availability, AuditLogs,
          Login, BotSimulator
✅ Hooks: useRooms, useBookings,
          useAvailability, useAuditLogs
✅ Context: AuthContext
✅ Components: Layout, Common, ErrorBoundary
✅ Validation: Zod + react-hook-form
✅ State: React Query

### Infrastructure
✅ Docker: Backend + Frontend + MySQL
✅ CI/CD: GitHub Actions
✅ Profiles: dev, local, prod

### Pending Activation
⏳ Microsoft Entra ID (needs Azure)
⏳ Teams Bot (needs Azure Bot Service)
⏳ Graph Calendar Sync (needs Graph perms)

### Testing
✅ Frontend: 82 tests (Vitest + React Testing Library)
   - Components: Badge, Pagination, StatCard, ErrorBoundary, ProtectedRoute
   - Pages: Dashboard, Login, Availability, Rooms, Bookings, AuditLogs
   - Context: AuthContext
   - Hooks: useRooms, useAvailability
   - Utils: dateUtils, validationSchemas
   - Smoke test
✅ Backend: 23+ tests (JUnit 5 + Mockito + MockMvc)
   - Controllers: RoomController, BookingController, AuditLogController, BotController, BotSimulatorController
   - Services: RoomServiceImpl, BookingServiceImpl, AuditService
   - Repositories: RoomRepository, BookingRepository
   - Bot: BotCommandParser, AdaptiveCardBuilder, MultiTurnDialogHandler
   - Graph: GraphServiceStub
✅ CI/CD: GitHub Actions runs frontend + backend tests on push
