CREATE TABLE IF NOT EXISTS users (
  id bigint NOT NULL AUTO_INCREMENT,
  email varchar(255) UNIQUE NOT NULL,
  password varchar(255),
  first_name varchar(100) NOT NULL,
  last_name varchar(100) NOT NULL,
  gender varchar(10) NOT NULL,
  phone varchar(20),
  birthdate date,
  avatar_url varchar(255),
  header_photo_url varchar(255),
  hometown VARCHAR(255),
  current_city VARCHAR(255),
  verified boolean NOT NULL DEFAULT false,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  provider varchar(20) NOT NULL DEFAULT('LOCAL'),
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS verification_tokens (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  token varchar(255),
  expired_at timestamp NOT NULL,
  PRIMARY KEY (id)
);

ALTER TABLE verification_tokens
ADD CONSTRAINT FK_tokens_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS `groups` (
  id bigint NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  description text,
  img_url varchar(255),
  color varchar(7) DEFAULT('#FFFFFF'),
  is_private boolean NOT NULL DEFAULT false,
  created_by bigint,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE `groups`
ADD CONSTRAINT FK_groups_created_by FOREIGN KEY (created_by)
REFERENCES users (id) ON DELETE SET NULL ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS group_join_requests (
  id bigint NOT NULL AUTO_INCREMENT,
  group_id bigint NOT NULL,
  user_id bigint NOT NULL COMMENT 'User to be added',
  initiated_by bigint NOT NULL COMMENT 'Could be the same if you are adding by yourself',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status varchar(20) DEFAULT('PENDING'),
  PRIMARY KEY (id)
);

ALTER TABLE group_join_requests
ADD CONSTRAINT FK_group_requests_group_id FOREIGN KEY (group_id)
REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE group_join_requests
ADD CONSTRAINT FK_group_requests_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE group_join_requests
ADD CONSTRAINT FK_group_requests_initiated_by FOREIGN KEY (initiated_by)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS group_members (
  id bigint NOT NULL AUTO_INCREMENT,
  group_id bigint NOT NULL,
  user_id bigint NOT NULL,
  role varchar(20) DEFAULT 'MEMBER',
  joined_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE group_members
ADD CONSTRAINT FK_group_members_group_id FOREIGN KEY (group_id)
REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE group_members
ADD CONSTRAINT FK_group_members_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS posts (
  id bigint NOT NULL AUTO_INCREMENT,
  description text,
  user_id bigint NOT NULL,
  group_id bigint,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE posts
ADD CONSTRAINT FK_posts_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE posts
ADD CONSTRAINT FK_posts_group_id FOREIGN KEY (group_id)
REFERENCES `groups` (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS post_images (
  id bigint NOT NULL AUTO_INCREMENT,
  post_id bigint NOT NULL,
  image_url varchar(255),
  PRIMARY KEY (id)
);

ALTER TABLE post_images
ADD CONSTRAINT FK_post_images_post_id FOREIGN KEY (post_id)
REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS likes (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  post_id bigint NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE likes
ADD CONSTRAINT FK_likes_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE likes
ADD CONSTRAINT FK_likes_post_id FOREIGN KEY (post_id)
REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS comments (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  post_id bigint NOT NULL,
  text text NOT NULL COMMENT 'Content of the post',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE comments
ADD CONSTRAINT FK_comments_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE comments
ADD CONSTRAINT FK_comments_post_id FOREIGN KEY (post_id)
REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS reposts (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  post_id bigint NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE reposts
ADD CONSTRAINT FK_reposts_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE reposts
ADD CONSTRAINT FK_reposts_post_id FOREIGN KEY (post_id)
REFERENCES posts (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS friends (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  friend_id bigint NOT NULL,
  status ENUM ('PENDING', 'ACCEPTED', 'DECLINED') DEFAULT ('PENDING'),
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  accepted_at timestamp,
  PRIMARY KEY (id)
);

ALTER TABLE friends
ADD CONSTRAINT FK_friends_user_id FOREIGN KEY (user_id)
REFERENCES users (id);

ALTER TABLE friends
ADD CONSTRAINT FK_friends_friend_id FOREIGN KEY (friend_id)
REFERENCES users (id);

CREATE TABLE IF NOT EXISTS followers (
  id bigint NOT NULL AUTO_INCREMENT,
  follower_id bigint NOT NULL COMMENT 'The one who subscribes',
  following_id bigint NOT NULL COMMENT 'The one who is subscribed to',
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE followers
ADD CONSTRAINT FK_followers_follower_id FOREIGN KEY (follower_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE followers
ADD CONSTRAINT FK_followers_following_id FOREIGN KEY (following_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

CREATE TABLE IF NOT EXISTS messages (
  id bigint NOT NULL AUTO_INCREMENT,
  sender_id bigint NOT NULL,
  receiver_id bigint NOT NULL,
  text text NOT NULL,
  is_read boolean NOT NULL DEFAULT false,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE messages
ADD CONSTRAINT FK_messages_sender_id FOREIGN KEY (sender_id)
REFERENCES users (id);

ALTER TABLE messages
ADD CONSTRAINT FK_messages_receiver_id FOREIGN KEY (receiver_id)
REFERENCES users (id);

CREATE TABLE IF NOT EXISTS notifications (
  id bigint NOT NULL AUTO_INCREMENT,
  type ENUM ('LIKE', 'COMMENT', 'REPOST', 'FRIEND', 'MESSAGE', 'BIRTHDAY', 'GROUP'),
  is_read boolean NOT NULL DEFAULT false,
  user_id bigint NOT NULL,
  related_user_id bigint,
  related_post_id bigint,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE notifications
ADD CONSTRAINT FK_notifications_user_id FOREIGN KEY (user_id)
REFERENCES users (id);

ALTER TABLE notifications
ADD CONSTRAINT FK_notifications_related_user_id FOREIGN KEY (related_user_id)
REFERENCES users (id);

ALTER TABLE notifications
ADD CONSTRAINT FK_notifications_related_post_id FOREIGN KEY (related_post_id)
REFERENCES posts (id);