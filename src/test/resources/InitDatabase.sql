CREATE SCHEMA IF NOT EXISTS userdata AUTHORIZATION SA;

SET SCHEMA userdata;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  fullname VARCHAR(255),
  email VARCHAR(100),
  dpassword CHAR(40)
);

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT PRIMARY KEY,
  username VARCHAR(50),
  user_role VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users (id),
  FOREIGN KEY (username) REFERENCES users (username)
);

INSERT INTO users (username, email, dpassword) VALUES ('Вася', 'vasya@na.com', '12345');

INSERT INTO users (username, email, dpassword) VALUES ('admin', 'admin@email.com', '123');

INSERT INTO user_roles (user_id, username, user_role) SELECT id, username, 'authenticated-user, manager-gui' FROM users WHERE username='admin';

INSERT INTO user_roles (user_id, username, user_role) SELECT id, username, 'authenticated-user' FROM users WHERE username='Вася';



