CREATE TABLE IF NOT EXISTS achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL UNIQUE,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_achievements (
  id bigint NOT NULL AUTO_INCREMENT,
  user_id bigint NOT NULL,
  achievement_id bigint NOT NULL,
  created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

ALTER TABLE user_achievements
ADD CONSTRAINT FK_user_achievements_user_id FOREIGN KEY (user_id)
REFERENCES users (id) ON DELETE CASCADE ON UPDATE NO ACTION;

ALTER TABLE user_achievements
ADD CONSTRAINT FK_user_achievements_achievement_id FOREIGN KEY (achievement_id)
REFERENCES achievements (id) ON DELETE CASCADE ON UPDATE NO ACTION;

INSERT INTO achievements (name, description, is_premium) VALUES
 ('Sweet & Signed In', 'First login', false),
 ('Pink Profile', 'Completed your profile + added avatar', false),
 ('Sugar Rush', 'Logged in 3 days in a row', false),
 ('You are Invited!', 'Invited a friend to Buzzly', false),

 ('Buzz Started', 'Your first post', false),
 ('First Heartbeat', 'Got your first like', false),
 ('Vibe Creator', '10 likes on one post', false),
 ('Comment King', '25 comments left', false),
 ('Soft Supporter', 'Replied to someone’s post with kindness', false),
 ('Tag Me Later', 'Tagged in a post or photo', false),

 ('Sweet Talker', 'Started 3 conversations in a day', false),
 ('Aesthetic Drop', 'Uploaded 5 aesthetic photos', false),
 ('Main Character Energy', 'Got 100 profile visits', false),
 ('Whimsical Wonder', 'Used 5+ different emoji reactions', false),
 ('Kind Soul', 'Reacted to 10 posts with like or heart', false),

 ('Buzzlight Star', 'Post went mini-viral (100+ likes)', false),
 ('Night Scroller', 'Online after 2:00 AM', false),
 ('Vanished & Reborn', 'Returned after 30+ days offline', false),
 ('Trend Starter', 'Used a hashtag that others adopted', false),
 ('Buzz Royalty', 'Top user of the month', false),

 ('Bee Baby', 'Bought your first 10 Bees', true),
 ('Golden Buzz', 'Purchased something from the premium shop', true),
 ('Buzz Gifter', 'Sent 5 virtual gifts to other users', true),
 ('Style King', 'Bought a profile customization', true),
 ('Premium Player', 'Active premium subscription holder', true);

 ALTER TABLE posts CHANGE description text TEXT;
