CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    name VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'USER'
);

-- Insert test users with BCrypt encoded passwords
-- Password for all users is "password123"
INSERT INTO users (username, password, email, name, role) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'admin@example.com', 'Admin User', 'ADMIN'),
('superadmin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'superadmin@example.com', 'Super Admin', 'SUPER_ADMIN'),
('user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'user@example.com', 'Regular User', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Verify data
SELECT * FROM users;