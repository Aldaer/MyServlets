CREATE SCHEMA IF NOT EXISTS userdata AUTHORIZATION SA;

SET SCHEMA userdata;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  fullname VARCHAR(255),
  email VARCHAR(100),
  dpassword CHAR(80)
);

CREATE TABLE IF NOT EXISTS user_roles (
  username VARCHAR(50),
  user_role VARCHAR(50),
  FOREIGN KEY (username) REFERENCES users (username) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT pkey PRIMARY KEY (username, user_role)
);

/* password = 12345 */
INSERT INTO users (username, email, dpassword) VALUES ('Вася', 'vasya@na.com', '8f0f8316092d572e1cf30fe6e52e628985977cc6bfbc7a05da17c1a67531f409bf37c130ae4f8cac');

/* password = 123 */
INSERT INTO users (username, email, dpassword) VALUES ('admin', 'admin@email.com', '1e25d00f056d40b65fe6b95d5ac56cac6d6217973ab8ccbe37ebbd031918d7f75dfe236380f84060');

INSERT INTO user_roles (username, user_role) VALUES ('admin', 'authenticated-user');
INSERT INTO user_roles (username, user_role) VALUES ('admin', 'manager-gui');
INSERT INTO user_roles (username, user_role) VALUES ('Вася', 'authenticated-user');

update users SET dpassword='43e3f6d9c10f8ae88f4d0900dc2e11fb0608fb1910f391699360bb32279cbb5e6a2f42032e958509' where username='Вася';


