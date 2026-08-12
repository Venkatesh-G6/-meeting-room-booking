CREATE TABLE IF NOT EXISTS employees (
  id           BIGINT AUTO_INCREMENT 
               PRIMARY KEY,
  name         VARCHAR(100) NOT NULL,
  email        VARCHAR(200) NOT NULL UNIQUE,
  department   VARCHAR(100),
  active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at   DATETIME NOT NULL 
               DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
  id           BIGINT AUTO_INCREMENT 
               PRIMARY KEY,
  room_name    VARCHAR(100) NOT NULL,
  capacity     INT NOT NULL,
  location     VARCHAR(200),
  status       ENUM('AVAILABLE','NA') 
               NOT NULL DEFAULT 'AVAILABLE',
  created_at   DATETIME NOT NULL 
               DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bookings (
  id           BIGINT AUTO_INCREMENT 
               PRIMARY KEY,
  room_id      BIGINT NOT NULL,
  employee_id  BIGINT NOT NULL,
  title        VARCHAR(200),
  start_time   DATETIME NOT NULL,
  end_time     DATETIME NOT NULL,
  status       ENUM('CONFIRMED','CANCELLED') 
               NOT NULL DEFAULT 'CONFIRMED',
  created_at   DATETIME NOT NULL 
               DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_booking_room 
    FOREIGN KEY (room_id) 
    REFERENCES rooms(id),
  CONSTRAINT fk_booking_employee 
    FOREIGN KEY (employee_id) 
    REFERENCES employees(id)
);

CREATE INDEX idx_bookings_room_date
  ON bookings(room_id, start_time);

CREATE INDEX idx_bookings_employee
  ON bookings(employee_id);

CREATE INDEX idx_bookings_start_time
  ON bookings(start_time);
