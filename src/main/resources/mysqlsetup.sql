DROP DATABASE IF EXISTS userdatabase;
CREATE DATABASE userdatabase COLLATE utf8_general_ci;
USE userdatabase;

CREATE TABLE credentials (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  dpassword CHAR(80)
);

CREATE TABLE temp_credentials (
  username VARCHAR(50) NOT NULL PRIMARY KEY,
  created BIGINT
);

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fullname VARCHAR(255),
  email VARCHAR(100),
  username VARCHAR(50),
  regcomplete BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (username) REFERENCES credentials(username) ON DELETE CASCADE
);

CREATE TABLE user_roles (
  username VARCHAR(50),
  user_role VARCHAR(50),
  FOREIGN KEY (username) REFERENCES credentials (username) ON DELETE CASCADE,
  CONSTRAINT pkey PRIMARY KEY (username, user_role)
);

CREATE TABLE messages (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  refid BIGINT NOT NULL DEFAULT 0,
  u_from VARCHAR(50) NOT NULL,
  u_to VARCHAR(50),
  m_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP(),
  conversation_id BIGINT NOT NULL DEFAULT 0,
  text VARCHAR(10000) DEFAULT ''
);

CREATE TABLE friends (
  uid BIGINT,
  fid BIGINT,
  FOREIGN KEY (uid) REFERENCES users (id) ON DELETE CASCADE,
  FOREIGN KEY (fid) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fkey PRIMARY KEY (uid, fid)
);

CREATE TABLE conversations (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  starter VARCHAR(50),
  started TIMESTAMP DEFAULT CURRENT_TIMESTAMP()
);

CREATE TABLE conversation_participants (
  convid BIGINT,
  uid    BIGINT,
  FOREIGN KEY (convid) REFERENCES conversations(id) ON DELETE CASCADE,
  FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT convkey PRIMARY KEY (convid, uid)
);

CREATE TABLE conversation_invited (
  convid BIGINT,
  uid BIGINT,
  FOREIGN KEY (convid) REFERENCES conversations(id) ON DELETE CASCADE,
  FOREIGN KEY (uid) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT invkey PRIMARY KEY (convid, uid)
);
