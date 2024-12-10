INSERT INTO mjga."user" ("username", "password")
VALUES ('admin', '$2a$10$7zfEdqQYJrBnmDdu7UkgS.zOAsJf4bB1ZYrVhCBAIvIoPbEmeVnVe');

INSERT INTO "mjga"."role" ("code", "name")
VALUES ('ADMIN', 'ADMIN'),
       ('GENERAL', 'GENERAL');

INSERT INTO "mjga"."permission" ("code", "name")
VALUES ('WRITE_USER_ROLE_PERMISSION', 'WRITE_USER_ROLE_PERMISSION'),
       ('READ_USER_ROLE_PERMISSION', 'READ_USER_ROLE_PERMISSION');

INSERT INTO "mjga"."user_role_map" ("user_id", "role_id")
VALUES (1, 1);

INSERT INTO "mjga"."role_permission_map" ("role_id", "permission_id")
VALUES (1, 1),
       (1, 2);
