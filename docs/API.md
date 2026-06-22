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
