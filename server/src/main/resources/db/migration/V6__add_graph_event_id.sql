-- Add Graph calendar event ID to bookings table
-- graph_event_id stores Microsoft Graph
-- calendar event ID for calendar sync.
-- NULL when Graph sync not enabled.
-- Populated by GraphService in prod.
ALTER TABLE bookings
ADD COLUMN graph_event_id
  VARCHAR(500) NULL
  AFTER status;

CREATE INDEX idx_bookings_graph_event
  ON bookings(graph_event_id);
