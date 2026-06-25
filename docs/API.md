# Meeting Room Booking API Documentation

> **Living Document:** This file is updated whenever a new API endpoint is added to the project.

## Base URL

```
http://localhost:8080
```

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

## Standard Response Format

All API responses follow the `ApiResponse` wrapper:

```json
{
  "success": true,
  "message": "Operation completed",
  "data": { ... },
  "timestamp": "2026-06-22T12:34:26.1207129"
}
```

## Room Management

### Check Room Availability

- **Method:** `GET`
- **URL:** `/api/rooms/available?date={date}&startTime={time}&endTime={time}&minCapacity={n}`
- **Description:** Returns all active rooms available for the given date and time slot, filtered by minimum capacity.
- **Query Parameters:**
  - `date` (required) — ISO date, e.g. `2026-06-27`
  - `startTime` (required) — ISO time, e.g. `10:00:00`
  - `endTime` (required) — ISO time, e.g. `11:00:00`
  - `minCapacity` (optional, default `1`) — minimum room capacity
- **Response:** `200 OK`
- **Response Message:** `Available rooms fetched successfully`
- **Response Data:**

```json
{
  "date": "2026-06-27",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "availableRooms": [ { "id": 1, "roomName": "Conference Room A", "capacity": 10, "...": "..." } ],
  "totalAvailable": 1
}
```

- **Validation Rules:**
  - `startTime` must be before `endTime`
  - `date` cannot be in the past
  - `minCapacity` must be at least 1
- **Note:** Rooms with a confirmed booking overlapping the requested slot are excluded. Adjacent (back-to-back) slots are not considered conflicts.

### Create Room

- **Method:** `POST`
- **URL:** `/api/rooms`
- **Description:** Creates a new meeting room.
- **Request Body:**

```json
{
  "roomName": "Conference Room A",
  "roomType": "MEETING",
  "capacity": 10,
  "location": "First Floor"
}
```

- **Response:** `201 CREATED`
- **Response Message:** `Room created successfully`
- **Validation Rules:**
  - `roomName` is required
  - `roomType` is required (`MEETING`, `TRAINING`, or `POD`)
  - `capacity` is required and must be at least 1

### Get All Active Rooms

- **Method:** `GET`
- **URL:** `/api/rooms`
- **Description:** Returns a list of all active rooms.
- **Response:** `200 OK`
- **Response Message:** `Rooms fetched successfully`

### Get Room by ID

- **Method:** `GET`
- **URL:** `/api/rooms/{id}`
- **Description:** Returns a single room by its ID.
- **Response:** `200 OK`
- **Response Message:** `Room fetched successfully`
- **Error:** `404 NOT FOUND` if room does not exist

### Update Room

- **Method:** `PUT`
- **URL:** `/api/rooms/{id}`
- **Description:** Updates room details.
- **Request Body:** Same as Create Room
- **Response:** `200 OK`
- **Response Message:** `Room updated successfully`
- **Error:** `404 NOT FOUND` if room does not exist
- **Validation Rules:** Same as Create Room

### Disable Room

- **Method:** `PATCH`
- **URL:** `/api/rooms/{id}/disable`
- **Description:** Marks a room as inactive.
- **Response:** `200 OK`
- **Response Message:** `Room disabled successfully`
- **Error:** `404 NOT FOUND` if room does not exist

## Booking Management

### Create Booking

- **Method:** `POST`
- **URL:** `/api/bookings`
- **Description:** Books a room for a specific time slot.
- **Request Body:**

```json
{
  "roomId": 1,
  "bookedBy": "user@example.com",
  "title": "Project Meeting",
  "attendeeCount": 5,
  "startTime": "2026-06-22T10:00:00",
  "endTime": "2026-06-22T11:00:00"
}
```

- **Response:** `201 CREATED`
- **Response Message:** `Room booked successfully`
- **Validation Rules:**
  - `roomId` is required
  - `bookedBy` is required
  - `attendeeCount` is required and must be at least 1
  - `startTime` and `endTime` are required
  - `startTime` must be before `endTime`
  - `startTime` cannot be in the past
  - `attendeeCount` cannot exceed room capacity
  - Room must not be double booked for overlapping time slots

### Get All Bookings

- **Method:** `GET`
- **URL:** `/api/bookings`
- **Description:** Returns a list of all bookings.
- **Response:** `200 OK`
- **Response Message:** `Bookings fetched successfully`

### Get Booking by ID

- **Method:** `GET`
- **URL:** `/api/bookings/{id}`
- **Description:** Returns a single booking by its ID.
- **Response:** `200 OK`
- **Response Message:** `Booking fetched successfully`
- **Error:** `404 NOT FOUND` if booking does not exist

### Get My Bookings

- **Method:** `GET`
- **URL:** `/api/bookings/my?bookedBy={email}`
- **Description:** Returns all bookings for a specific user, ordered by start time descending.
- **Response:** `200 OK`
- **Response Message:** `My bookings fetched successfully`

### Cancel Booking

- **Method:** `DELETE`
- **URL:** `/api/bookings/{id}?requestedBy={email}`
- **Description:** Cancels a booking. Only the user who booked it can cancel.
- **Response:** `200 OK`
- **Response Message:** `Booking cancelled successfully`
- **Error:** `400 BAD REQUEST` if already cancelled or if `requestedBy` does not match `bookedBy`

## Utility & Monitoring

### Swagger UI

- **Method:** `GET`
- **URL:** `/swagger-ui.html`
- **Description:** Interactive API documentation interface.

### OpenAPI Spec

- **Method:** `GET`
- **URL:** `/v3/api-docs`
- **Description:** Machine-readable OpenAPI JSON specification.

### Health Check

- **Method:** `GET`
- **URL:** `/actuator/health`
- **Description:** Application health status.

## Change Log

| Date | Changes |
|---|---|
| 2026-06-22 | Added Room Management endpoints (POST, GET, GET by ID, PUT, PATCH disable) |
| 2026-06-22 | Added Booking Management endpoints (POST, GET, GET by ID, GET my bookings, DELETE cancel) with conflict detection |
| 2026-06-22 | Added Check Room Availability endpoint (GET /api/rooms/available) with capacity filter and overlap exclusion |
