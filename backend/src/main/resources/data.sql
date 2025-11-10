-- Initial data for The Hive platform

-- USERS
INSERT INTO users (id, email, password_hash, name, bio, province, district, geohash, role, balance_hours, created_at, updated_at)
VALUES
(1, 'alice@example.com', 'hash123', 'Alice Aydın', 'Love volunteering!', 'Istanbul', 'Kadıköy', 'sxk3', 'USER', 3, NOW(), NOW()),
(2, 'bob@example.com', 'hash456', 'Bob Balcı', 'Community builder', 'Ankara', 'Çankaya', 'syet', 'USER', 5, NOW(), NOW()),
(3, 'admin@example.com', 'hash789', 'Admin User', 'Platform moderator', 'Istanbul', 'Beşiktaş', 'sxk9', 'ADMIN', 10, NOW(), NOW());

-- OFFERS
INSERT INTO offers (id, provider_id, title, description, duration_hours, start_date, end_date, province, district, geohash, status, created_at, updated_at)
VALUES
(1, 1, 'Gardening Help', 'Assist with small garden maintenance.', 2, '2025-11-01', '2025-11-10', 'Istanbul', 'Kadıköy', 'sxk3', 'ACTIVE', NOW(), NOW());

-- REQUESTS
INSERT INTO requests (id, seeker_id, title, description, duration_hours, start_date, end_date, province, district, geohash, status, created_at, updated_at)
VALUES
(1, 2, 'Dog Walking', 'Looking for someone to walk my dog.', 1, '2025-11-02', '2025-11-03', 'Ankara', 'Çankaya', 'syet', 'ACTIVE', NOW(), NOW());

-- SEMANTIC TAGS
INSERT INTO semantic_tags (id, name, wikidata_id)
VALUES
(1, 'Gardening', 'Q11023'),
(2, 'Pets', 'Q144');

-- TAG LINKS
INSERT INTO offer_tags (offer_id, tag_id) VALUES (1, 1);
INSERT INTO request_tags (request_id, tag_id) VALUES (1, 2);

-- HANDSHAKES
INSERT INTO handshakes (id, offer_id, seeker_id, provider_id, status, agreed_hours, created_at, completed_at)
VALUES
(1, 1, 2, 1, 'COMPLETED', 2, NOW() - INTERVAL '2 days', NOW());

-- TIMEBANK TRANSACTIONS
INSERT INTO timebank_transactions (id, sender_id, receiver_id, handshake_id, amount, description, created_at)
VALUES
(1, 1, 2, 1, 2, 'Time exchange for gardening help', NOW());

-- RATINGS
INSERT INTO ratings (id, handshake_id, rater_id, ratee_id, punctuality, friendliness, communicative, preparedness, comment, created_at)
VALUES
(1, 1, 2, 1, 5, 5, 4, 5, 'Very reliable and friendly!', NOW());

-- BADGES
INSERT INTO badges (id, name, description, icon_url)
VALUES
(1, 'New Comer', 'Awarded for joining the community.', 'https://example.com/icons/newcomer.png'),
(2, 'Experienced User', 'Awarded after completing 3 or more exchanges.', 'https://example.com/icons/experienced.png');

-- USER BADGES
INSERT INTO user_badges (user_id, badge_id, earned_at)
VALUES
(1, 1, NOW()),   -- Alice is new
(2, 2, NOW());   -- Bob is experienced

-- FORUM TOPICS
INSERT INTO forum_topics (id, author_id, title, created_at, updated_at)
VALUES
(1, 2, 'Welcome to The Hive!', NOW(), NOW()),
(2, 1, 'Tips for New Volunteers', NOW(), NOW()),
(3, 3, 'Platform Updates & Feedback', NOW(), NOW()),
(4, 2, 'Local Events in November', NOW(), NOW());

-- FORUM POSTS
INSERT INTO forum_posts (id, topic_id, author_id, content, created_at)
VALUES
(1, 1, 1, 'Happy to be part of this community!', NOW()),
(2, 1, 2, 'Glad to have you here, Alice!', NOW()),
(3, 2, 2, 'Always start with small commitments to build trust.', NOW()),
(4, 2, 1, 'That''s good advice — thank you, Bob!', NOW()),
(5, 3, 3, 'We''ve rolled out minor bug fixes and performance improvements.', NOW()),
(6, 3, 1, 'App feels smoother now, nice work!', NOW()),
(7, 4, 2, 'Anyone joining the community cleanup on Saturday?', NOW()),
(8, 4, 1, 'I''ll be there with some friends!', NOW()),
(9, 4, 3, 'Great initiative, folks. Keep it up.', NOW());

-- MESSAGES
INSERT INTO messages (id, sender_id, receiver_id, offer_id, content, created_at)
VALUES
(1, 2, 1, 1, 'Thanks again for helping with my garden!', NOW());

-- Reset sequences to avoid conflicts with future inserts
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('offers_id_seq', (SELECT MAX(id) FROM offers));
SELECT setval('requests_id_seq', (SELECT MAX(id) FROM requests));
SELECT setval('semantic_tags_id_seq', (SELECT MAX(id) FROM semantic_tags));
SELECT setval('handshakes_id_seq', (SELECT MAX(id) FROM handshakes));
SELECT setval('timebank_transactions_id_seq', (SELECT MAX(id) FROM timebank_transactions));
SELECT setval('ratings_id_seq', (SELECT MAX(id) FROM ratings));
SELECT setval('badges_id_seq', (SELECT MAX(id) FROM badges));
SELECT setval('forum_topics_id_seq', (SELECT MAX(id) FROM forum_topics));
SELECT setval('forum_posts_id_seq', (SELECT MAX(id) FROM forum_posts));
SELECT setval('messages_id_seq', (SELECT MAX(id) FROM messages));
