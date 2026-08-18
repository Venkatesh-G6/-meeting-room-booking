# MeetSpace — Microsoft Teams Tab Package

This folder holds the Teams app package for embedding MeetSpace as a personal tab.

## Before you can sideload the app

1. **Host the app on a public HTTPS domain.** Teams cannot load `localhost`.
   Deploy the frontend (and backend API) to Azure App Service or another host.
2. **Update `manifest.json`:**
   - Replace `id` with a real GUID (e.g. from `[guid]::NewGuid()` in PowerShell,
     or any GUID generator).
   - Replace every `REPLACE_WITH_YOUR_HOSTED_DOMAIN` with your real domain
     (no `https://` prefix in `validDomains`, but include it in `contentUrl`).
   - Update `developer` fields with real company info and URLs.
3. **Add icons to this folder:**
   - `color.png` — 192×192px, full-color app icon.
   - `outline.png` — 32×32px, transparent outline icon.
   (No icon files exist in the repo yet — only SVGs under `client/room-booking-ui/public`,
   which need to be exported to PNG at the sizes above.)
4. **Zip the contents** of this folder (`manifest.json`, `color.png`, `outline.png`)
   into `MeetSpace.zip` — the zip root must contain these files directly, not a
   subfolder.

## Sideload into Teams

1. Open Microsoft Teams → Apps → Manage your apps → Upload an app → Upload
   a custom app.
2. Select `MeetSpace.zip`.
3. Add it to your personal Teams.

## Notes

- The backend already sends a `Content-Security-Policy: frame-ancestors`
  header permitting `teams.microsoft.com` / `*.office.com` domains, so Teams
  can embed the app once it's hosted publicly.
- If you later want Teams single sign-on (no separate login screen), configure
  a real Azure AD app registration (see `server/.env.example`) and add a
  `webApplicationInfo` block to this manifest.
