# MeetSpace — Meeting Room Booking System

A full-stack meeting room booking application with React frontend and Spring Boot backend, designed for Microsoft Teams integration.

## Tech Stack

- **Frontend:** React 18, TypeScript, Vite, TailwindCSS, React Query, React Hook Form + Zod
- **Backend:** Spring Boot 3, Java 17+, Maven, JPA/Hibernate, Flyway
- **Database:** MySQL (prod), H2 in-memory (dev)
- **Auth:** OAuth2 Resource Server, Azure AD (Entra ID) — pending activation
- **Bot:** Microsoft Bot Framework — pending activation
- **CI/CD:** GitHub Actions
- **Containerization:** Docker, Docker Compose

## Quick Start (Dev Mode — No Azure Required)

### Prerequisites

- Java 17+ (Java 21 recommended)
- Maven 3.9+
- Node 20+
- Git

### Backend

```bash
cd server
$env:SPRING_PROFILES_ACTIVE="dev"   # PowerShell
mvn spring-boot:run
```

The dev profile uses H2 in-memory database — no MySQL needed.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:roombooking_db`)

### Frontend

```bash
cd client/room-booking-ui
npm install
npm run dev
```

- App: http://localhost:5173
- Click **Dev Mode Login** on the login page for instant admin access

## Running Tests

### Frontend (82 tests)

```bash
cd client/room-booking-ui
npx vitest run              # run all tests
npx vitest run --coverage   # with coverage
npm run typecheck           # TypeScript check
npm run lint                # ESLint
```

### Backend (23+ tests)

```bash
cd server
$env:SPRING_PROFILES_ACTIVE="test"
mvn test
```

## Docker (Full Stack)

```bash
cp .env.example .env
# Edit .env: set DB_PASSWORD
docker compose up --build
```

- Frontend: http://localhost:80
- Backend: http://localhost:8080

## Project Structure

```
Meetingroom/
├── client/room-booking-ui/    # React frontend
│   ├── src/
│   │   ├── components/        # Reusable UI components
│   │   ├── context/           # AuthContext
│   │   ├── hooks/             # React Query hooks
│   │   ├── pages/             # Dashboard, Availability, Rooms, Bookings, AuditLogs, BotSimulator
│   │   └── test/              # Vitest + React Testing Library
│   └── package.json
├── server/                    # Spring Boot backend
│   ├── src/main/java/com/yourcompany/roombooking/
│   │   ├── audit/             # AuditService
│   │   ├── bot/               # Bot handler, command parser, adaptive cards
│   │   ├── config/            # Security, CORS, Bot config
│   │   ├── controller/        # REST controllers
│   │   ├── dto/               # Request/Response DTOs
│   │   ├── entity/            # JPA entities
│   │   ├── graph/             # Microsoft Graph API (stub + real)
│   │   ├── repository/        # JPA repositories
│   │   └── service/           # Business logic
│   └── pom.xml
├── .github/workflows/ci.yml   # CI/CD pipeline
├── docker-compose.yml         # Full stack Docker setup
├── ARCHITECTURE.md            # Architecture overview
├── DEVELOPER_SETUP.md         # Detailed setup guide
├── GRAPH_SETUP.md             # Microsoft Graph API setup
└── TEAMS_ACTIVATION.md        # Teams bot activation guide
```

## Environment Variables

| Variable | Required | Profile | Description |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | No | all | `dev` (default), `local`, `prod` |
| `DB_URL` | No | prod | MySQL JDBC URL |
| `DB_USERNAME` | No | prod | MySQL username |
| `DB_PASSWORD` | Yes | prod | MySQL password |
| `BOT_APP_ID` | No | dev | Azure Bot Service ID |
| `BOT_APP_PASSWORD` | No | dev | Azure Bot Service password |
| `AZURE_TENANT_ID` | Prod | prod | Entra ID tenant |
| `AZURE_CLIENT_ID` | Prod | prod | Entra ID client ID |
| `AZURE_CLIENT_SECRET` | Prod | prod | Entra ID client secret |
| `VITE_ENV` | No | frontend | `dev` or `production` |

## Documentation

- [Architecture Overview](ARCHITECTURE.md)
- [Developer Setup Guide](DEVELOPER_SETUP.md)
- [Graph API Setup](GRAPH_SETUP.md)
- [Teams Bot Activation](TEAMS_ACTIVATION.md)

## CI/CD

GitHub Actions pipeline runs on push to `main`, `develop`, and `Azure` branches:
1. Backend tests (Maven)
2. Backend build (JAR)
3. Frontend checks (typecheck, lint, unit tests, build)
4. Docker build (main branch only)
