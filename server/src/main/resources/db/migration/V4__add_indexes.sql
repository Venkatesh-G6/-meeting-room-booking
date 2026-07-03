CREATE INDEX idx_bookings_room_id
  ON bookings(room_id);

CREATE INDEX idx_bookings_status
  ON bookings(status);

CREATE INDEX idx_bookings_start_time
  ON bookings(start_time);

CREATE INDEX idx_audit_logs_actor
  ON audit_logs(actor_email);

CREATE INDEX idx_audit_logs_entity
  ON audit_logs(entity_type, entity_id);
