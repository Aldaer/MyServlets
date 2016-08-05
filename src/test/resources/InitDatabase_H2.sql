// Uncomment lines when creating embedded database instead of in-memory

// == H2 ==
//CREATE SCHEMA IF NOT EXISTS userdata AUTHORIZATION SA;
//SET SCHEMA userdata;

// == MySql ==
// create database userdatabase;
// use userdatabase;

CREATE ALIAS CURRENT_UTC_TIMESTAMP AS $$
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
@CODE
Timestamp ts() {
return Timestamp.valueOf(LocalDateTime.now(ZoneOffset.UTC));
}
$$;
DROP TABLE IF EXISTS credentials;
CREATE TABLE credentials (
  username VARCHAR_IGNORECASE(50) NOT NULL PRIMARY KEY,
  dpassword CHAR(80)
);

DROP TABLE IF EXISTS temp_credentials;
CREATE TABLE temp_credentials (
  username VARCHAR_IGNORECASE(50) NOT NULL PRIMARY KEY,
  created BIGINT
);

INSERT INTO temp_credentials (username, created) VALUES ('_perm_user', 4102444799000);

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fullname VARCHAR_IGNORECASE,
  email VARCHAR(100),
  username VARCHAR_IGNORECASE(50),
  regcomplete BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (username) REFERENCES credentials(username) ON DELETE CASCADE
);

DROP TABLE IF EXISTS user_roles;
CREATE TABLE user_roles (
  username VARCHAR_IGNORECASE(50),
  user_role VARCHAR(50),
  FOREIGN KEY (username) REFERENCES credentials (username) ON DELETE CASCADE,
  CONSTRAINT pkey PRIMARY KEY (username, user_role)
);

/* password = 12345 */
INSERT INTO credentials (username, dpassword) VALUES ('вася', '8f0f8316092d572e1cf30fe6e52e628985977cc6bfbc7a05da17c1a67531f409bf37c130ae4f8cac');
INSERT INTO users (username, fullname, email, regcomplete) VALUES ('вася', 'Василий Петров', 'vpetrov@email.com', TRUE);

/* password = 12345 */
INSERT INTO credentials (username, dpassword) VALUES ('петя', 'e751782f2cdfd0fb5a42cc375956e9e7e797731c0f0a1b36d5b84ca9a63e01ee35bbc5a806c273cd');
INSERT INTO users (username, fullname, email, regcomplete) VALUES ('петя', 'Петр Васечкин', 'p.vasechkin@email.com', TRUE);

/* password = 123 */
INSERT INTO credentials (username, dpassword) VALUES ('admin', 'e23fa8862aeea8d58a5726e4c365c96d0fe4bf055e88dd9ee2b41c7b885500386f3c3af390c4ef8e');
INSERT INTO users (username, email, regcomplete) VALUES ('admin', 'admin@somewhere.com', TRUE);

INSERT INTO user_roles (username, user_role) VALUES ('admin', 'authenticated-user');
INSERT INTO user_roles (username, user_role) VALUES ('admin', 'manager-gui');
INSERT INTO user_roles (username, user_role) VALUES ('admin', 'admin-gui');
INSERT INTO user_roles (username, user_role) VALUES ('вася', 'authenticated-user');
INSERT INTO user_roles (username, user_role) VALUES ('петя', 'authenticated-user');

DROP TABLE IF EXISTS messages;
CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  refid BIGINT NOT NULL DEFAULT 0,
  u_from VARCHAR_IGNORECASE(50) NOT NULL,
  u_to VARCHAR_IGNORECASE(50),
  m_time TIMESTAMP NOT NULL DEFAULT CURRENT_UTC_TIMESTAMP(),
  conversation_id BIGINT NOT NULL DEFAULT 0,
  text VARCHAR DEFAULT ''
);

INSERT INTO messages (u_from, u_to, text, m_time) VALUES ('вася', 'петя', 'Привет, Петя!', '2015-01-01 12:00:00');
INSERT INTO messages (u_from, u_to, refid, text, m_time) VALUES ('петя', 'вася', identity(), 'И тебе привет!', '2015-01-01 12:10:00');
INSERT INTO messages (u_from, u_to, text, m_time) VALUES ('вася', 'non existing user', 'Письмо никому', '2015-01-01 13:00:00');

INSERT INTO messages (u_from, conversation_id, text, m_time) VALUES ('вася', 1, 'Письмо в сообщество', '2015-01-02 12:00:00');
INSERT INTO messages (u_from, conversation_id, refid, text, m_time) VALUES ('петя', 1, identity(), 'А я прочитал!', '2015-01-03 12:00:00');

DROP TABLE IF EXISTS friends;
CREATE TABLE friends (
  uid BIGINT,
  fid BIGINT,
  FOREIGN KEY (uid) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (fid) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fkey PRIMARY KEY (uid, fid)
);

INSERT INTO friends (uid, fid) values((SELECT TOP 1 id FROM users WHERE username='вася'), (SELECT TOP 1 id FROM users WHERE username='петя'));
INSERT INTO friends (uid, fid) values((SELECT TOP 1 id FROM users WHERE username='петя'), (SELECT TOP 1 id FROM users WHERE username='вася'));

DROP TABLE IF EXISTS conversations;
CREATE TABLE conversations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR_IGNORECASE(100) NOT NULL,
  description VARCHAR(255),
  starter VARCHAR_IGNORECASE(50),
  started TIMESTAMP DEFAULT CURRENT_UTC_TIMESTAMP()
);

INSERT INTO conversations (id, name, description) VALUES (1, 'Сообщество', 'Просто поболтать');

DROP TABLE IF EXISTS conversation_participants;
CREATE TABLE conversation_participants (
  convid BIGINT,
  uid BIGINT,
  FOREIGN KEY (convid) REFERENCES conversations(id) ON DELETE CASCADE,
  FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT convkey PRIMARY KEY (convid, uid)
);

INSERT INTO conversation_participants (convid, uid) values (1, (SELECT TOP 1 id FROM users WHERE username='вася'));
INSERT INTO conversation_participants (convid, uid) values (1, (SELECT TOP 1 id FROM users WHERE username='петя'));
