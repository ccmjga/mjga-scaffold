CREATE SCHEMA IF NOT EXISTS mjga;

CREATE TABLE mjga.user (
                           id BIGSERIAL PRIMARY KEY,
                           username VARCHAR NOT NULL UNIQUE,
                           create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           password VARCHAR NOT NULL,
                           enable BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE mjga.permission (
                                 id BIGSERIAL PRIMARY KEY,
                                 code VARCHAR NOT NULL UNIQUE,
                                 name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE mjga.role (
                           id BIGSERIAL PRIMARY KEY,
                           code VARCHAR NOT NULL UNIQUE,
                           name VARCHAR NOT NULL UNIQUE
);

CREATE TABLE mjga.role_permission_map (
                                          role_id BIGINT NOT NULL,
                                          permission_id BIGINT NOT NULL,
                                          PRIMARY KEY (role_id, permission_id),
                                          FOREIGN KEY (role_id) REFERENCES mjga.role(id) ON DELETE CASCADE,
                                          FOREIGN KEY (permission_id) REFERENCES mjga.permission(id) ON DELETE CASCADE
);

CREATE TABLE mjga.user_role_map (
                                    user_id BIGINT NOT NULL,
                                    role_id BIGINT NOT NULL,
                                    PRIMARY KEY (user_id, role_id),
                                    FOREIGN KEY (user_id) REFERENCES mjga.user(id) ON DELETE CASCADE,
                                    FOREIGN KEY (role_id) REFERENCES mjga.role(id) ON DELETE CASCADE
);