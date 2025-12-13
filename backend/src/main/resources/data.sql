-- Initial data for The Hive platform
-- This script is idempotent and can be run multiple times safely

-- USERS
INSERT INTO users (id, email, password_hash, name, bio, province, district, geohash, role, balance_hours, created_at, updated_at)
VALUES
(1, 'alice@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Alice Aydın', 'Love volunteering!', 'Istanbul', 'Kadıköy', 'sxk3', 'USER', 3, NOW(), NOW()),
(2, 'bob@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Bob Balcı', 'Community builder', 'Ankara', 'Çankaya', 'syet', 'USER', 5, NOW(), NOW()),
(3, 'admin@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Admin User', 'Platform moderator', 'Istanbul', 'Beşiktaş', 'sxk9', 'ADMIN', 10, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- OFFERS
INSERT INTO offers (id, provider_id, title, description, duration_hours, start_date, end_date, province, district, geohash, status, created_at, updated_at)
VALUES
(1, 1, 'Gardening Help', 'Assist with small garden maintenance.', 2, '2025-11-01', '2025-11-10', 'Istanbul', 'Kadıköy', 'sxk3', 'ACTIVE', NOW(), NOW()),
(2, 2, 'Programming Tutoring', 'Help with Python and JavaScript basics.', 3, '2025-11-01', '2025-12-01', 'Istanbul', 'Kadıköy', 'sxk3', 'ACTIVE', NOW(), NOW()),
(3, 3, 'Photography Services', 'Event and portrait photography.', 4, '2025-11-05', '2025-12-05', 'Istanbul', 'Beşiktaş', 'sxk9', 'ACTIVE', NOW(), NOW()),
(4, 2, 'Language Exchange', 'Practice Turkish and English conversation.', 2, '2025-11-10', '2025-12-10', 'Istanbul', 'Şişli', 'sxk3', 'ACTIVE', NOW(), NOW()),
(5, 3, 'Home Cooking Classes', 'Learn traditional Turkish dishes.', 3, '2025-11-01', '2025-11-30', 'Istanbul', 'Kadıköy', 'sxk3', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- REQUESTS
-- NOTE: Request IDs start at 6 to avoid conflicts with offer IDs (1-5)
INSERT INTO requests (id, seeker_id, title, description, duration_hours, start_date, end_date, province, district, geohash, status, created_at, updated_at)
VALUES
(6, 2, 'Dog Walking', 'Looking for someone to walk my dog.', 1, '2025-11-02', '2025-11-03', 'Ankara', 'Çankaya', 'syet', 'ACTIVE', NOW(), NOW()),
(7, 3, 'Math Tutoring', 'Need help with high school mathematics.', 2, '2025-11-05', '2025-12-05', 'Istanbul', 'Kadıköy', 'sxk3', 'ACTIVE', NOW(), NOW()),
(8, 2, 'Bike Repair', 'Need someone to fix my bicycle.', 1, '2025-11-08', '2025-11-15', 'Istanbul', 'Beşiktaş', 'sxk9', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- SEMANTIC TAGS
INSERT INTO semantic_tags (id, name, wikidata_id)
VALUES
(1, 'Gardening', 'Q11023'),
(2, 'Pets', 'Q144'),
(3, 'Programming', 'Q80006'),
(4, 'Education', 'Q8434'),
(5, 'Photography', 'Q11633'),
(6, 'Language', 'Q34770'),
(7, 'Cooking', 'Q38695'),
(8, 'Mathematics', 'Q395'),
(9, 'Repair', 'Q43015')
ON CONFLICT (id) DO NOTHING;

-- TAG LINKS
INSERT INTO offer_tags (offer_id, tag_id) VALUES (1, 1) ON CONFLICT DO NOTHING;
INSERT INTO offer_tags (offer_id, tag_id) VALUES (2, 3) ON CONFLICT DO NOTHING;
INSERT INTO offer_tags (offer_id, tag_id) VALUES (2, 4) ON CONFLICT DO NOTHING;
INSERT INTO offer_tags (offer_id, tag_id) VALUES (3, 5) ON CONFLICT DO NOTHING;
INSERT INTO offer_tags (offer_id, tag_id) VALUES (4, 6) ON CONFLICT DO NOTHING;
INSERT INTO offer_tags (offer_id, tag_id) VALUES (4, 4) ON CONFLICT DO NOTHING;
INSERT INTO offer_tags (offer_id, tag_id) VALUES (5, 7) ON CONFLICT DO NOTHING;
INSERT INTO request_tags (request_id, tag_id) VALUES (6, 2) ON CONFLICT DO NOTHING;
INSERT INTO request_tags (request_id, tag_id) VALUES (7, 8) ON CONFLICT DO NOTHING;
INSERT INTO request_tags (request_id, tag_id) VALUES (7, 4) ON CONFLICT DO NOTHING;
INSERT INTO request_tags (request_id, tag_id) VALUES (8, 9) ON CONFLICT DO NOTHING;

-- HANDSHAKES
INSERT INTO handshakes (id, offer_id, seeker_id, provider_id, status, duration_hours, created_at, agreed_at)
VALUES
(1, 1, 2, 1, 'COMPLETED', 2, NOW() - INTERVAL '2 days', NOW()),
(2, 2, 3, 2, 'COMPLETED', 3, NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days'),
(3, 3, 1, 3, 'COMPLETED', 4, NOW() - INTERVAL '10 days', NOW() - INTERVAL '5 days'),
(4, 5, 2, 3, 'COMPLETED', 3, NOW() - INTERVAL '7 days', NOW() - INTERVAL '3 days'),
(5, 4, 1, 2, 'PENDING', 2, NOW() - INTERVAL '1 day', NULL)
ON CONFLICT (id) DO NOTHING;

-- TIMEBANK TRANSACTIONS
INSERT INTO timebank_transactions (id, sender_id, receiver_id, handshake_id, amount, description, created_at)
VALUES
(1, 1, 2, 1, 2, 'Time exchange for gardening help', NOW()),
(2, 2, 3, 2, 3, 'Time exchange for programming tutoring', NOW() - INTERVAL '2 days'),
(3, 3, 1, 3, 4, 'Time exchange for photography services', NOW() - INTERVAL '5 days'),
(4, 3, 2, 4, 3, 'Time exchange for cooking classes', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- RATINGS
INSERT INTO ratings (id, handshake_id, rater_id, ratee_id, punctuality, friendliness, communicative, preparedness, comment, created_at)
VALUES
(1, 1, 2, 1, 5, 5, 4, 5, 'Very reliable and friendly!', NOW()),
(2, 1, 1, 2, 5, 4, 5, 4, 'Great experience helping Bob with his garden. He was very appreciative!', NOW()),
(3, 2, 3, 2, 5, 5, 5, 5, 'Bob is an excellent tutor! Very patient and explained concepts clearly. Highly recommend!', NOW() - INTERVAL '2 days'),
(4, 2, 2, 3, 4, 5, 4, 4, 'Admin was a quick learner and asked great questions. Pleasure to teach!', NOW() - INTERVAL '2 days'),
(5, 3, 1, 3, 5, 5, 5, 5, 'Amazing photographer! Captured beautiful moments at my event. Very professional!', NOW() - INTERVAL '5 days'),
(6, 3, 3, 1, 5, 4, 5, 5, 'Alice was wonderful to work with. Clear about what she wanted and very organized.', NOW() - INTERVAL '5 days'),
(7, 4, 2, 3, 4, 5, 4, 5, 'The cooking class was fantastic! Learned so much about Turkish cuisine. Will definitely attend again!', NOW() - INTERVAL '3 days'),
(8, 4, 3, 2, 5, 5, 5, 4, 'Bob was enthusiastic and engaged during the class. Made teaching a joy!', NOW() - INTERVAL '3 days')
ON CONFLICT (id) DO NOTHING;

-- BADGES
INSERT INTO badges (id, name, description, icon_url)
VALUES
(1, 'New Comer', 'Awarded for joining the community.', 'https://example.com/icons/newcomer.png'),
(2, 'Experienced User', 'Awarded after completing 3 or more exchanges.', 'https://example.com/icons/experienced.png')
ON CONFLICT (id) DO NOTHING;

-- USER BADGES
INSERT INTO user_badges (user_id, badge_id, earned_at)
VALUES
(1, 1, NOW()),   -- Alice is new
(2, 2, NOW())     -- Bob is experienced
ON CONFLICT DO NOTHING;

-- FORUM TOPICS
INSERT INTO forum_topics (id, author_id, title, created_at, updated_at)
VALUES
(1, 2, 'Welcome to The Hive!', NOW(), NOW()),
(2, 1, 'Tips for New Volunteers', NOW(), NOW()),
(3, 3, 'Platform Updates & Feedback', NOW(), NOW()),
(4, 2, 'Local Events in November', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

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
(9, 4, 3, 'Great initiative, folks. Keep it up.', NOW())
ON CONFLICT (id) DO NOTHING;

-- MESSAGES
INSERT INTO messages (id, sender_id, receiver_id, offer_id, content, created_at)
VALUES
(1, 2, 1, 1, 'Thanks again for helping with my garden!', NOW())
ON CONFLICT (id) DO NOTHING;

-- QUESTIONS
INSERT INTO questions (id, offer_id, request_id, asker_id, content, created_at)
VALUES
(1, 2, NULL, 1, 'Hi Bob! I''m interested in the programming tutoring. Do you cover web development frameworks like React?', NOW() - INTERVAL '3 days'),
(2, 3, NULL, 2, 'What type of camera equipment do you use for portrait photography?', NOW() - INTERVAL '4 days'),
(3, 5, NULL, 1, 'How many people can attend the cooking class at once? Is it one-on-one or a group session?', NOW() - INTERVAL '6 days'),
(4, 4, NULL, 3, 'What level of English proficiency do you have? I''m looking to practice advanced conversation.', NOW() - INTERVAL '2 days'),
(5, NULL, 7, 1, 'What grade level math are we talking about? I might be able to help with high school algebra and geometry.', NOW() - INTERVAL '4 days'),
(6, NULL, 8, 1, 'What kind of bike do you have? I have some experience with road bikes and mountain bikes.', NOW() - INTERVAL '5 days'),
(7, 1, NULL, 3, 'Do you also help with indoor plants or just outdoor gardening?', NOW() - INTERVAL '7 days'),
(8, 2, NULL, 3, 'How long have you been programming? And do you teach beginners?', NOW() - INTERVAL '8 days')
ON CONFLICT (id) DO NOTHING;

-- ANSWERS
INSERT INTO answers (id, question_id, responder_id, content, created_at)
VALUES
(1, 1, 2, 'Yes! I cover React, Vue, and also Node.js for backend. We can focus on what you''re most interested in learning.', NOW() - INTERVAL '3 days' + INTERVAL '2 hours'),
(2, 2, 3, 'I use a Canon EOS R5 with prime lenses. For portraits, I typically use an 85mm f/1.4 lens. Professional quality guaranteed!', NOW() - INTERVAL '4 days' + INTERVAL '1 hour'),
(3, 3, 3, 'I prefer keeping classes intimate with max 3-4 people so everyone gets hands-on experience. But I can also do one-on-one if you prefer!', NOW() - INTERVAL '6 days' + INTERVAL '3 hours'),
(4, 4, 2, 'I''d say I''m at an upper-intermediate to advanced level. I lived in the US for 2 years, so I''m comfortable with complex topics and idioms.', NOW() - INTERVAL '2 days' + INTERVAL '5 hours'),
(5, 5, 3, 'It''s 10th grade level - algebra II and some trigonometry. That would be perfect if you can help with those topics!', NOW() - INTERVAL '4 days' + INTERVAL '4 hours'),
(6, 6, 2, 'It''s a mountain bike with disc brakes. The gears are acting up. If you have experience with that, it would be great!', NOW() - INTERVAL '5 days' + INTERVAL '2 hours'),
(7, 7, 1, 'Both! I have experience with houseplants as well. I can help with repotting, pest control, and general care tips.', NOW() - INTERVAL '7 days' + INTERVAL '1 hour'),
(8, 8, 2, 'I''ve been coding for about 5 years professionally. Yes, I specialize in teaching beginners! I believe anyone can learn to code with the right guidance.', NOW() - INTERVAL '8 days' + INTERVAL '3 hours')
ON CONFLICT (id) DO NOTHING;

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
SELECT setval('questions_id_seq', (SELECT MAX(id) FROM questions));
SELECT setval('answers_id_seq', (SELECT MAX(id) FROM answers));
