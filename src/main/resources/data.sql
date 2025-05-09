CREATE TABLE IF NOT EXISTS users (
  id bigint NOT NULL AUTO_INCREMENT,
  email varchar(255) UNIQUE NOT NULL,
  password varchar(255) NOT NULL,
  first_name varchar(100) NOT NULL,
  last_name varchar(100) NOT NULL,
  gender ENUM ('MALE', 'FEMALE', 'CUSTOM') NOT NULL,
  phone_number varchar(20),
  birth_date date,
  avatar_url varchar(255),
  header_photo varchar(255),
  hometown VARCHAR(255),
  current_city VARCHAR(255),
  verified boolean NOT NULL DEFAULT false,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  provider ENUM ('LOCAL', 'GOOGLE') NOT NULL COMMENT 'For login, to know what a provider was',
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS verification_tokens (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  token varchar(255),
  expired_at timestamp NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE verification_tokens ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS `groups` (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  description text,
  is_private boolean NOT NULL DEFAULT false,
  created_by bigint,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE `groups` ADD FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS group_join_requests (
  id bigint NOT NULL AUTO_INCREMENT,
  group_id bigint NOT NULL,
  user_id bigint NOT NULL COMMENT 'User to be added',
  initiated_by bigint NOT NULL COMMENT 'Could be the same if you are adding by yourself',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status ENUM ('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT ('PENDING'),
  PRIMARY KEY (id)
);

ALTER TABLE group_join_requests ADD FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE group_join_requests ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE group_join_requests ADD FOREIGN KEY (initiated_by) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS group_members (
  id bigint NOT NULL AUTO_INCREMENT,
  group_id bigint NOT NULL,
  user_id bigint NOT NULL,
  role ENUM ('ADMIN', 'MODERATOR', 'MEMBER') NOT NULL DEFAULT ('MEMBER'),
  joined_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE group_members ADD FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE group_members ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS posts (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  group_id bigint,
  body text COMMENT 'Content of the post',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE posts ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE posts ADD FOREIGN KEY (group_id) REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS post_images (
  id bigint NOT NULL AUTO_INCREMENT,
  post_id bigint NOT NULL,
  image_url varchar(255),
  PRIMARY KEY (id)
);

ALTER TABLE post_images ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS likes (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  post_id bigint NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE likes ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE likes ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS comments (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  post_id bigint NOT NULL,
  body text NOT NULL COMMENT 'Content of the post',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE comments ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE comments ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS reposts (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  post_id bigint NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE reposts ADD FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE reposts ADD FOREIGN KEY (post_id) REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS friends (
  id bigint NOT NULL AUTO_INCREMENT,
  sender_id bigint NOT NULL,
  receiver_id bigint NOT NULL,
  status ENUM ('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT ('PENDING'),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accepted_at timestamp,
  PRIMARY KEY (id)
);

ALTER TABLE friends ADD FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE friends ADD FOREIGN KEY (receiver_id) REFERENCES users (id);

CREATE TABLE IF NOT EXISTS followers (
  id bigint NOT NULL AUTO_INCREMENT,
  follower_id bigint NOT NULL COMMENT 'The one who subscribes',
  following_id bigint NOT NULL COMMENT 'The one who is subscribed to',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE followers ADD FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE followers ADD FOREIGN KEY (following_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS messages (
  id bigint NOT NULL AUTO_INCREMENT,
  sender_id bigint NOT NULL,
  receiver_id bigint NOT NULL,
  body text,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE messages ADD FOREIGN KEY (sender_id) REFERENCES users (id);

ALTER TABLE messages ADD FOREIGN KEY (receiver_id) REFERENCES users (id);

CREATE TABLE IF NOT EXISTS notifications (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  type ENUM ('FRIEND', 'LIKE', 'COMMENT', 'MESSAGE', 'GROUP'),
  related_user_id bigint,
  related_post_id bigint,
  is_read boolean NOT NULL DEFAULT false,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE notifications ADD FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE notifications ADD FOREIGN KEY (related_user_id) REFERENCES users (id);

ALTER TABLE notifications ADD FOREIGN KEY (related_post_id) REFERENCES posts (id);
