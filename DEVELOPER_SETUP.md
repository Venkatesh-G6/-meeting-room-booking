# Developer Setup Guide

## Prerequisites
- Java 21
- Maven 3.9+
- Node 20+
- MySQL 8.0
- Docker Desktop
- Git

## Quick Start (Dev Mode — No Azure needed)

### Step 1 — Clone Repository
git clone <repo-url>
cd roombooking

### Step 2 — Backend Environment
Copy .env.example to configure:

Create .vscode/launch.json:
{
  "type": "java",
  "name": "RoombookingApplication",
  "request": "launch",
  "mainClass": 
    "com.yourcompany.roombooking
     .RoombookingApplication",
  "env": {
    "SPRING_PROFILES_ACTIVE": "dev",
    "DB_URL": "jdbc:mysql://localhost:3306/roombooking_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC",
    "DB_USERNAME": "root",
    "DB_PASSWORD": "YOUR_MYSQL_PASSWORD",
    "BOT_APP_ID": "dev-bot-id",
    "BOT_APP_PASSWORD": "dev-bot-password"
  }
}

Note: .vscode/ is gitignored.
Never commit real passwords.

### Step 3 — Start MySQL
Option A: Local MySQL on port 3306
Option B: Docker MySQL only:
  docker compose up mysql

### Step 4 — Start Backend
mvn spring-boot:run
Verify: http://localhost:8080/swagger-ui.html

### Step 5 — Frontend Environment
cd room-booking-ui
cp .env.example .env
Edit .env: set VITE_ENV=dev

npm install
npm run dev
Verify: http://localhost:5173

### Step 6 — Dev Login
Click "Dev Mode Login" on login page.
Full admin access granted automatically.

## Docker Setup (Full Stack)
cd docker/
cp .env.example .env
Edit .env: set DB_PASSWORD

docker compose up --build
Verify: http://localhost:80

## Running Tests
Backend:
mvn clean test -Dspring.profiles.active=dev

Frontend:
npm run test
npm run test:coverage

Both:
npm run typecheck
npm run lint

## Environment Variables Reference

Variable             Required  Default   Notes
────────────────────────────────────────────
DB_URL               No        localhost Full JDBC URL
DB_USERNAME          No        root      MySQL user
DB_PASSWORD          YES       none      MySQL password
SPRING_PROFILES      No        dev       dev/local/prod
BOT_APP_ID           No        dev-id    Azure Bot ID
BOT_APP_PASSWORD     No        dev-pass  Azure Bot pass
AZURE_TENANT_ID      Prod only none      Entra ID
AZURE_CLIENT_ID      Prod only none      Entra ID
AZURE_CLIENT_SECRET  Prod only none      Entra ID
AZURE_APP_ID_URI     Prod only none      Entra ID

## Profile Guide
dev   → No auth, stub Graph, dev bot
local → Real Entra ID, stub Graph
prod  → Full auth, real Graph, real bot

## Common Issues

Issue: Could not resolve placeholder
Fix: Check all env vars are set in
     Windsurf run config

Issue: Flyway migration failed
Fix: Drop roombooking_db and restart

Issue: CORS error in browser
Fix: Verify backend running on 8080
     Verify VITE_ENV=dev in .env

Issue: Bot simulator not responding
Fix: Check /api/messages/simulate
     endpoint is accessible
     Only available in dev profile
