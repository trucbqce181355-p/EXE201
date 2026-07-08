-- Thêm các cột timestamp cho bảng users (nếu bảng đã tồn tại thiếu các cột này)
-- Chạy từng lệnh một; nếu báo "Duplicate column" thì cột đó đã có, bỏ qua.

ALTER TABLE users ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN password_changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
