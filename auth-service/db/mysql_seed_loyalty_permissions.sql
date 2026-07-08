USE auth_db;

INSERT IGNORE INTO permissions (name, resource, action, description) VALUES
('LOYALTY:CONFIG', 'LOYALTY', 'CONFIG', 'Configure loyalty rules and tiers'),
('LOYALTY:MANAGE_BENEFITS', 'LOYALTY', 'MANAGE_BENEFITS', 'Manage loyalty tier benefits');

INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.name IN ('LOYALTY:CONFIG', 'LOYALTY:MANAGE_BENEFITS')
WHERE r.name IN ('ADMIN', 'MANAGER');
