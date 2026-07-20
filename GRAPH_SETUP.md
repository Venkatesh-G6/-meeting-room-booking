# Microsoft Graph API Setup Guide

## Overview
This document describes how to configure
Microsoft Graph API integration for
calendar sync and meeting notifications.

## Current Status
Dev profile: STUB mode (no real API calls)
Prod profile: Ready for configuration

## Prerequisites
1. Azure App Registration (see AZURE_SETUP.md)
2. Graph API Permissions in Azure Portal

## Required Graph Permissions
Go to Azure Portal → App Registration
→ API Permissions → Add Permission
→ Microsoft Graph → Application Permissions

Add these permissions:
- Calendars.ReadWrite
  (Create and manage calendar events)
- User.Read.All  
  (Read user profiles)
- Mail.Send
  (Send meeting invites)
- OnlineMeetings.ReadWrite
  (Create Teams meetings)

Click "Grant admin consent"

## Environment Variables Required
AZURE_TENANT_ID=your-tenant-id
AZURE_CLIENT_ID=your-client-id
AZURE_CLIENT_SECRET=your-client-secret

## Activation Steps
1. Complete Azure App Registration
2. Grant Graph API permissions
3. Set environment variables
4. Switch to local or prod profile
5. GraphServiceImpl activates automatically
6. Test with POST /api/bookings
7. Verify calendar event in Outlook

## Testing Graph Integration
1. Create a booking via API
2. Check Outlook calendar for event
3. Cancel the booking
4. Verify event removed from Outlook

## Troubleshooting
Error: "Graph API not yet configured"
→ Check Azure credentials are set
→ Verify profile is local or prod

Error: "Insufficient privileges"  
→ Check Graph permissions granted
→ Verify admin consent given

Error: "Calendar not found"
→ Verify room email exists in Exchange
→ Check Calendars.ReadWrite permission
