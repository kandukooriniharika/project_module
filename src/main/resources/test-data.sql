-- Test Users for RBAC System
-- Note: Passwords are BCrypt encoded for "password123"
-- Use these for testing the Role-Based Access Control system

-- Insert test users
INSERT INTO users (name, email, username, password, created_at, updated_at) VALUES
('Admin User', 'admin@company.com', 'admin', '$2a$10$DowJones2024RBAC.encoded.password.hash.here', NOW(), NOW()),
('John Developer', 'john.dev@company.com', 'johndev', '$2a$10$DowJones2024RBAC.encoded.password.hash.here', NOW(), NOW()),
('Jane ProductOwner', 'jane.po@company.com', 'janepo', '$2a$10$DowJones2024RBAC.encoded.password.hash.here', NOW(), NOW()),
('Mike ScrumMaster', 'mike.sm@company.com', 'mikesm', '$2a$10$DowJones2024RBAC.encoded.password.hash.here', NOW(), NOW()),
('Sarah MultiRole', 'sarah@company.com', 'sarahmulti', '$2a$10$DowJones2024RBAC.encoded.password.hash.here', NOW(), NOW());

-- Insert roles for users (assuming user IDs are auto-generated 1-5)
-- Admin role for Admin User
INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');

-- Developer role for John
INSERT INTO user_roles (user_id, role) VALUES (2, 'DEVELOPER');

-- Product Owner role for Jane  
INSERT INTO user_roles (user_id, role) VALUES (3, 'PRODUCT_OWNER');

-- Scrum Master role for Mike
INSERT INTO user_roles (user_id, role) VALUES (4, 'SCRUM_MASTER');

-- Multiple roles for Sarah (Developer + Scrum Master)
INSERT INTO user_roles (user_id, role) VALUES (5, 'DEVELOPER');
INSERT INTO user_roles (user_id, role) VALUES (5, 'SCRUM_MASTER');

-- Note: To generate actual BCrypt password hashes, you can use:
-- 1. Online BCrypt generators
-- 2. Spring Boot application with PasswordEncoder bean
-- 3. Command: python3 -c "import bcrypt; print(bcrypt.hashpw(b'password123', bcrypt.gensalt()).decode())"

-- Example test queries to verify RBAC:
-- SELECT u.username, ur.role FROM users u JOIN user_roles ur ON u.id = ur.user_id;
-- SELECT * FROM users WHERE username = 'admin';

-- Test login credentials:
-- Username: admin,     Password: password123 (ADMIN role)
-- Username: johndev,   Password: password123 (DEVELOPER role)  
-- Username: janepo,    Password: password123 (PRODUCT_OWNER role)
-- Username: mikesm,    Password: password123 (SCRUM_MASTER role)
-- Username: sarahmulti, Password: password123 (DEVELOPER + SCRUM_MASTER roles)