# Meeting Room Booking Automation System

## 1. Overview

The Meeting Room Booking Automation System is an enterprise application that enables employees to book shared resources such as meeting rooms, training rooms, and pods without manual intervention.

The system integrates with Microsoft Teams and the Microsoft Graph API to provide a seamless booking and calendar experience.

Primary objectives:

* Eliminate manual room booking processes
* Prevent double bookings
* Provide real-time room availability visibility
* Maintain booking history and audit logs
* Support Teams-based booking workflows
* Serve as a generic Resource Booking Platform for future resource types

---

## 2. Architecture Principles

The application must follow:

* Domain Driven Design (DDD)
* Separation of Concerns
* Clean Architecture
* API First Design
* Database as Source of Truth
* Stateless Services
* Extensible Resource Booking Model

The system is designed as a **Resource Booking Platform** rather than only a Room Booking Platform.

Future resources may include:

* Meeting Rooms
* Training Rooms
* Pods
* Projectors
* Parking Slots
* Laptops

---

## 3. High Level Architecture

Employee
↓
Microsoft Teams
↓
Teams Bot
↓
Booking API
↓
Application Layer
↓
Domain Layer
↓
Persistence Layer
↓
MySQL

External Integrations:

Booking API
↓
Microsoft Graph API
↓
Outlook Calendar
↓
Teams Notifications

---

## 4. System Components

## Teams Integration Layer

Responsibilities:

* Receive user commands
* Trigger booking workflows
* Display confirmations
* Display errors

Examples:

Book Room A tomorrow 11am

Show available rooms

Cancel my booking

---

## Booking API Layer

Responsibilities:

* Request validation
* Authentication
* Authorization
* Route requests to application services

Technology:

* Spring Boot REST APIs

---

## Application Layer

Responsibilities:

* Execute use cases
* Coordinate domain services
* Manage transactions

Examples:

BookRoomUseCase

CancelBookingUseCase

GetAvailabilityUseCase

---

## Domain Layer

Responsibilities:

* Business rules
* Validation
* Booking policies

Domain Objects:

Resource

Room

Booking

User

AuditLog

---

## Persistence Layer

Responsibilities:

* Database operations
* Query optimization
* Transaction management

Technology:

* Spring Data JPA
* MySQL

---

## Integration Layer

Responsibilities:

* Microsoft Graph integration
* Teams notifications
* Calendar synchronization

---

## 5. Domain Model

## Resource (Base Aggregate)

Attributes:

* id
* name
* resourceType (MEETING_ROOM, TRAINING_ROOM, POD, etc.)
* capacity
* timezone
* active
* location
* createdAt
* updatedAt

Rules:

* Resource must be active to accept bookings
* Resource capacity must be greater than attendee count
* Resource timezone is used to interpret local booking times

## Room

A specialization of Resource.

Additional Attributes:

* roomNumber
* floor
* building
* amenities

Rules:

* Room must be active to accept bookings
* Room capacity must be greater than attendee count

## Booking

Attributes:

* id
* resourceId
* bookedBy
* attendeeCount
* startTime
* endTime
* status
* calendarSyncStatus
* createdAt
* updatedAt
* version (optimistic locking)

Status Values:

* PENDING
* CONFIRMED
* CANCELLED
* SYNC_FAILED

Calendar Sync Status Values:

* NOT_SYNCED
* SYNCED
* SYNC_FAILED

Rules:

* Cannot overlap with existing active booking
* Start time must be before end time
* Cannot be booked in the past
* Canceled bookings remain in the system for audit

## User

Attributes:

* id
* email
* displayName
* role
* entraId
* active
* createdAt
* updatedAt

Role Values:

* ROLE_EMPLOYEE
* ROLE_ADMIN

Rules:

* User must be active to perform bookings
* Role determines authorization scope

## Audit Log

Attributes:

* id
* action
* entityType
* entityId
* performedBy
* details
* timestamp

---

## 6. Business Rules

BR-001

Room cannot be double booked.

BR-002

Booking start time must be earlier than end time.

BR-003

Past date bookings are not allowed.

BR-004

Inactive rooms cannot be booked.

BR-005

Only booking owner or administrator may cancel booking.

BR-006

Room capacity must support attendee count.

BR-007

Every booking action must be audited.

BR-008

System must be transaction safe.

BR-009

Canceled bookings are retained for audit and must not be hard deleted.

BR-010

All booking times are interpreted in the resource's configured timezone.

BR-011

Adjacent bookings without buffer are allowed unless a room policy defines a buffer.

---

## 7. Failure Handling

## Concurrent Booking Requests

Problem:

Two users attempt to book the same room simultaneously.

Solution:

* Database-level unique constraint on (resource_id, start_time, end_time) where status is active
* Optimistic locking using `version` column on booking
* Pessimistic locking on availability reads

## Notification Failure

Problem:

Booking succeeds but Teams notification fails.

Solution:

* Booking remains successful
* Notification is stored in an Outbox table
* Background worker retries delivery asynchronously

## Calendar Sync Failure

Problem:

Microsoft Graph unavailable.

Solution:

* Store sync status on booking
* Retry using scheduled job with exponential backoff
* Alert admin after repeated failures

## Invalid Booking Window

Problem:

End time earlier than start time.

Solution:

* Reject request at API and domain layer
* Return 400 Bad Request with a clear error message

---

## 8. Database Design

## resource

Fields:

* id (PK, BIGINT AUTO_INCREMENT)
* name (VARCHAR)
* resource_type (VARCHAR)
* capacity (INT)
* timezone (VARCHAR)
* active (BOOLEAN)
* location (VARCHAR, optional)
* created_at (TIMESTAMP)
* updated_at (TIMESTAMP)
* version (INT, optimistic locking)

Indexes:

* active, resource_type

## room

Fields:

* id (PK, BIGINT)
* resource_id (FK to resource)
* room_number (VARCHAR)
* floor (VARCHAR)
* building (VARCHAR)
* amenities (JSON)

Indexes:

* resource_id

## booking

Fields:

* id (PK, BIGINT AUTO_INCREMENT)
* resource_id (FK to resource)
* booked_by (FK to user)
* attendee_count (INT)
* start_time (TIMESTAMP)
* end_time (TIMESTAMP)
* status (VARCHAR)
* calendar_sync_status (VARCHAR)
* created_at (TIMESTAMP)
* updated_at (TIMESTAMP)
* version (INT)

Indexes:

* resource_id, start_time, end_time
* booked_by
* status

Constraints:

* Unique constraint or exclusion check for overlapping active bookings per resource

## user

Fields:

* id (PK, BIGINT AUTO_INCREMENT)
* email (VARCHAR, UNIQUE)
* display_name (VARCHAR)
* role (VARCHAR)
* entra_id (VARCHAR, UNIQUE)
* active (BOOLEAN)
* created_at (TIMESTAMP)
* updated_at (TIMESTAMP)

Indexes:

* email
* entra_id

## audit_log

Fields:

* id (PK, BIGINT AUTO_INCREMENT)
* action (VARCHAR)
* entity_type (VARCHAR)
* entity_id (VARCHAR)
* performed_by (VARCHAR)
* details (JSON)
* created_at (TIMESTAMP)

Indexes:

* entity_type, entity_id
* created_at

## outbox

Fields:

* id (PK, BIGINT AUTO_INCREMENT)
* event_type (VARCHAR)
* payload (JSON)
* status (VARCHAR)
* retry_count (INT)
* scheduled_at (TIMESTAMP)
* created_at (TIMESTAMP)

Indexes:

* status, scheduled_at

---

## 9. API Design

## Room APIs

POST /api/v1/rooms

GET /api/v1/rooms

GET /api/v1/rooms/{id}

PUT /api/v1/rooms/{id}

PATCH /api/v1/rooms/{id}/status

DELETE /api/v1/rooms/{id} (soft delete for admin)

## Booking APIs

POST /api/v1/bookings

PATCH /api/v1/bookings/{id}/cancel

GET /api/v1/bookings/my

GET /api/v1/bookings/{id}

GET /api/v1/bookings (admin only)

## Availability APIs

GET /api/v1/resources/available

Parameters:

* startAt (ISO-8601 timestamp)
* endAt (ISO-8601 timestamp)
* capacity
* resourceType (optional)

## Notification APIs

GET /api/v1/bookings/{id}/sync-status

POST /api/v1/bookings/{id}/retry-sync (admin)

## Common API Conventions

* Pagination using `page` and `size` query parameters
* Sorting using `sort` query parameter
* JSON request and response bodies
* ISO-8601 timestamps with timezone

---

## 10. Security Architecture

Authentication:

* Microsoft Entra ID (OAuth 2.0 / OpenID Connect)
* JWT token validation on every request
* Token refresh handled by Entra ID

Authorization:

* ROLE_EMPLOYEE
* ROLE_ADMIN

Access Control:

Employee:

* Book room
* Cancel own booking
* View own bookings

Admin:

* Manage resources and rooms
* View all bookings
* Cancel or override any booking
* Retry calendar sync

Additional Controls:

* Input validation using Bean Validation
* Rate limiting on public endpoints
* CORS policy for Teams integration
* HTTPS only

---

## 11. Project Structure

src/main/java/com/company/roombooking

├── domain
│   ├── resource
│   ├── room
│   ├── booking
│   ├── audit
│   └── user
│
├── application
│   ├── usecases
│   │   ├── booking
│   │   ├── room
│   │   └── availability
│   ├── services
│   └── dto
│
├── infrastructure
│   ├── persistence
│   ├── graph
│   ├── teams
│   ├── outbox
│   ├── scheduler
│   └── security
│
├── api
│   ├── controllers
│   ├── requests
│   └── responses
│
└── shared
    ├── exceptions
    ├── constants
    └── utilities

---

## 12. Development Phases

Phase 1

Core Booking Engine

* Resource and Room Management
* Booking APIs
* Conflict Detection
* Audit Logging

Phase 2

Security

* Microsoft Login
* Role Management
* JWT validation

Phase 3

Teams Integration

* Teams Bot
* Booking Commands

Phase 4

Microsoft Graph

* Calendar Events
* Teams Notifications
* Outbox-based retry

Phase 5

Advanced Features

* Recurring Meetings
* QR Check-In
* Analytics Dashboard

---

## 13. Success Criteria

The solution is successful when:

* Manual booking process eliminated
* Double bookings impossible
* Teams booking operational
* Calendar synchronization operational
* Audit trail available
* Booking workflow completed in less than 30 seconds

---

## 14. Explicit Non-Goals (Version 1)

The following features are NOT part of Version 1:

* AI Agent
* Natural Language Processing
* Predictive Room Recommendations
* Voice Booking
* Multi-Office Booking

These may be evaluated in future releases after the core platform is stable.

---

## 15. Observability & Deployment

## Deployment

* Containerized using Docker
* Orchestrated via Kubernetes or Azure Container Apps
* Database migrations managed by Flyway or Liquibase

## Observability

* Structured logging using SLF4J + Logstash
* Health check endpoint: GET /actuator/health
* Metrics exposed via Spring Boot Actuator + Micrometer
* Distributed tracing using OpenTelemetry

## CI/CD

* Git-based pipeline
* Automated unit and integration tests
* Static code analysis
* Container image build and push

---

## 16. Database Migration Order

1. Create `user` table
2. Create `resource` table
3. Create `room` table
4. Create `booking` table with conflict constraints
5. Create `audit_log` table
6. Create `outbox` table

---

## 17. Notes

* All timestamps are stored in UTC and converted to resource timezone when displayed.
* Soft delete is preferred over hard delete for all core entities.
* Optimistic locking is used for booking mutations to prevent lost updates.
* The Outbox pattern is used for reliable delivery of notifications and calendar sync events.
