-- Users table for Phase 9 Entra ID integration
-- azure_oid links to Microsoft Entra user object

CREATE TABLE IF NOT EXISTS users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  email         VARCHAR(200) NOT NULL UNIQUE,
  display_name  VARCHAR(200),
  role          ENUM('EMPLOYEE','ADMIN') NOT NULL DEFAULT 'EMPLOYEE',
  azure_oid     VARCHAR(100),
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  last_login    DATETIME,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_azure_oid ON users(azure_oid);
