-- Booking query performance
CREATE INDEX idx_bookings_room_id
  ON bookings(room_id);

CREATE INDEX idx_bookings_status
  ON bookings(status);

CREATE INDEX idx_bookings_start_time
  ON bookings(start_time);

CREATE INDEX idx_bookings_end_time
  ON bookings(end_time);

CREATE INDEX idx_bookings_booked_by
  ON bookings(booked_by);

-- Overlap detection performance
CREATE INDEX idx_bookings_overlap
  ON bookings(room_id, status, start_time, end_time);

-- Audit log query performance
CREATE INDEX idx_audit_actor
  ON audit_logs(actor_email);

CREATE INDEX idx_audit_entity
  ON audit_logs(entity_type, entity_id);

CREATE INDEX idx_audit_action
  ON audit_logs(action);

CREATE INDEX idx_audit_created
  ON audit_logs(created_at);
