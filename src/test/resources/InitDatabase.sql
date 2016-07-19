CREATE SCHEMA IF NOT EXISTS userdata AUTHORIZATION SA;

SET SCHEMA userdata;

CREATE TABLE IF NOT EXISTS credentials (
  username VARCHAR_IGNORECASE(50) NOT NULL PRIMARY KEY,
  dpassword CHAR(80)
);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fullname VARCHAR(255),
  email VARCHAR(100),
  username VARCHAR_IGNORECASE(50),
  FOREIGN KEY (username) REFERENCES credentials(username) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_roles (
  username VARCHAR_IGNORECASE(50),
  user_role VARCHAR(50),
  FOREIGN KEY (username) REFERENCES credentials (username) ON DELETE CASCADE,
  CONSTRAINT pkey PRIMARY KEY (username, user_role)
);

/* password = 12345 */
INSERT INTO credentials (username, dpassword) VALUES ('вася', '8f0f8316092d572e1cf30fe6e52e628985977cc6bfbc7a05da17c1a67531f409bf37c130ae4f8cac');
INSERT INTO users (username, fullname, email) VALUES ('вася', 'Василий Петров', 'vpetrov@email.com');

/* password = 12345 */
INSERT INTO credentials (username, dpassword) VALUES ('петя', 'e751782f2cdfd0fb5a42cc375956e9e7e797731c0f0a1b36d5b84ca9a63e01ee35bbc5a806c273cd');
INSERT INTO users (username, fullname, email) VALUES ('петя', 'Петр Васечкин', 'p.vasechkin@email.com');


/* password = 123 */
INSERT INTO credentials (username, dpassword) VALUES ('admin', 'e23fa8862aeea8d58a5726e4c365c96d0fe4bf055e88dd9ee2b41c7b885500386f3c3af390c4ef8e');
INSERT INTO users (username, email) VALUES ('admin', 'admin@somewhere.com');

INSERT INTO user_roles (username, user_role) VALUES ('admin', 'authenticated-user');
INSERT INTO user_roles (username, user_role) VALUES ('admin', 'manager-gui');
INSERT INTO user_roles (username, user_role) VALUES ('admin', 'admin-gui');
INSERT INTO user_roles (username, user_role) VALUES ('вася', 'authenticated-user');
INSERT INTO user_roles (username, user_role) VALUES ('петя', 'authenticated-user');


