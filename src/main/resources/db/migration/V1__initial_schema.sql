-- 초기 스키마 (엔티티 기준). 이후 스키마 변경 시 V2__, V3__ ... 마이그레이션 추가.

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE,
    generation INT,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    real_name VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    encrypt_password VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    tech_stack VARCHAR(255),
    github_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    blog_url VARCHAR(255),
    profile_image_key VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6)
);

CREATE TABLE chatroom (
    room_id VARCHAR(255) PRIMARY KEY,
    room_name VARCHAR(255),
    created_at DATETIME(6)
);

CREATE TABLE token (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    expiry_date DATETIME(6) NOT NULL,
    revoked BIT(1) NOT NULL,
    jti VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(255) NOT NULL
);

CREATE TABLE `role-change` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    real_name VARCHAR(255),
    email VARCHAR(255),
    previous_role VARCHAR(255),
    requested_role VARCHAR(255),
    request_status VARCHAR(255),
    processed_by VARCHAR(255),
    requested_at DATETIME(6),
    processed_at DATETIME(6),
    CONSTRAINT uk_email_role UNIQUE (email, previous_role, requested_role)
);

CREATE TABLE postlike (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_type VARCHAR(50),
    post_id BIGINT,
    username VARCHAR(255),
    created_at DATETIME(6),
    CONSTRAINT uk_user_post_type UNIQUE (username, post_id, post_type)
);

CREATE TABLE view (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_type VARCHAR(50),
    post_id BIGINT,
    view_count BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT uk_post UNIQUE (post_type, post_id)
);

CREATE TABLE view_daily_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    total_view_count BIGINT NOT NULL,
    increment_count BIGINT NOT NULL,
    created_at DATETIME(6)
);

CREATE TABLE alarms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_username VARCHAR(255),
    actor_username VARCHAR(255),
    domain_id BIGINT,
    alarm_type VARCHAR(255),
    domain_type VARCHAR(255),
    message VARCHAR(255),
    seen BIT(1),
    created_at DATETIME(6)
);

CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255),
    username VARCHAR(255),
    post_id BIGINT,
    post_type VARCHAR(50),
    parent_id BIGINT NULL,
    depth INT DEFAULT 0,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_id) REFERENCES comment (id)
);

CREATE TABLE chat_room_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chatroom_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    display_name VARCHAR(100),
    last_read_message_id BIGINT,
    CONSTRAINT fk_chat_room_user_room FOREIGN KEY (chatroom_id) REFERENCES chatroom (room_id),
    CONSTRAINT fk_chat_room_user_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE chat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_room_id VARCHAR(255),
    type VARCHAR(255),
    username VARCHAR(255),
    content VARCHAR(255),
    image_url VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    INDEX idx_room_created (chat_room_room_id, created_at DESC),
    CONSTRAINT fk_chat_room FOREIGN KEY (chat_room_room_id) REFERENCES chatroom (room_id)
);
