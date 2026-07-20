# Teams Bot Activation Guide

## Overview
All bot code is complete and tested.
This guide covers the steps to activate
the bot when Azure access is available.

## What Is Already Done
✅ TeamsBot.java - Complete
✅ BotCommandParser - Complete  
✅ AdaptiveCardBuilder - Complete
✅ /api/messages endpoint - Complete
✅ Bot Simulator UI - Complete
✅ All commands tested locally

## What You Need
1. Azure subscription
2. Teams admin access (app sideloading)
3. ngrok account (free tier works)

## Step 1 — Register Azure Bot (30 mins)
1. Go to portal.azure.com
2. Create resource → Azure Bot
3. Bot handle: RoomBookingBot
4. Pricing tier: F0 (Free)
5. Microsoft App ID: Create new
6. Copy App ID → BOT_APP_ID env var
7. Go to Configuration → Manage Password
8. New client secret → copy value
   → BOT_APP_PASSWORD env var

## Step 2 — Enable Teams Channel (10 mins)
1. Azure Bot → Channels
2. Add Microsoft Teams channel
3. Accept terms → Save

## Step 3 — Setup ngrok (15 mins)
1. Install: npm install -g ngrok
2. Run: ngrok http 8080
3. Copy HTTPS URL e.g.
   https://abc123.ngrok.io
4. Add to Azure Bot Configuration:
   Messaging endpoint:
   https://abc123.ngrok.io/api/messages

## Step 4 — Update Environment (5 mins)
Add to Windsurf run config:
BOT_APP_ID=your-real-app-id
BOT_APP_PASSWORD=your-real-password

Restart Spring Boot app.

## Step 5 — Create Teams App Package (20 mins)
Create manifest.json:
{
  "schema": "https://developer.microsoft.com/json-schemas/teams/v1.16/MicrosoftTeams.schema.json",
  "manifestVersion": "1.16",
  "version": "1.0.0",
  "id": "${BOT_APP_ID}",
  "packageName": "com.yourcompany.roombooking",
  "developer": {
    "name": "Your Company",
    "websiteUrl": "https://yourcompany.com",
    "privacyUrl": "https://yourcompany.com/privacy",
    "termsOfUseUrl": "https://yourcompany.com/terms"
  },
  "name": {
    "short": "Room Booking",
    "full": "Meeting Room Booking Bot"
  },
  "description": {
    "short": "Book meeting rooms",
    "full": "Book and manage meeting rooms directly from Teams"
  },
  "icons": {
    "outline": "outline.png",
    "color": "color.png"
  },
  "accentColor": "#0078D4",
  "bots": [{
    "botId": "${BOT_APP_ID}",
    "scopes": ["personal", "team"],
    "isNotificationOnly": false,
    "supportsCalling": false,
    "supportsVideo": false
  }],
  "permissions": ["identity", "messageTeamMembers"],
  "validDomains": ["*.ngrok.io"]
}

Create teams-app/ folder.
Add manifest.json.
Add two icon files:
  color.png (192x192)
  outline.png (32x32)
Zip the folder → RoomBookingBot.zip

## Step 6 — Install in Teams (10 mins)
1. Open Microsoft Teams
2. Apps → Manage your apps
3. Upload an app → Upload custom app
4. Select RoomBookingBot.zip
5. Add to personal chat

## Step 7 — Test in Teams (30 mins)
Open chat with Room Booking bot.
Test each command:
□ "hello" → help card
□ "check availability tomorrow 
   10am to 11am"
□ "my bookings"
□ "book meeting room a tomorrow 
   10am to 11am"
□ "cancel 1"

## Troubleshooting
Bot not responding:
→ Check ngrok is running
→ Check messaging endpoint URL
→ Check BOT_APP_ID and PASSWORD set
→ Check /api/messages endpoint live

401 Unauthorized:
→ Bot App ID or Password wrong
→ Re-check Azure Bot credentials

Card not rendering:
→ Check JSON is valid
→ Test card at adaptivecards.io

## After ngrok — Production Deployment
Replace ngrok URL with:
AWS ECS public URL or
Azure App Service URL

Update Teams App manifest validDomains
with production domain.
Republish Teams app package.
