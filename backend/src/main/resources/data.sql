-- Initial data for The Hive platform
-- This script is idempotent and can be run multiple times safely

-- USERS
INSERT INTO users (id, email, password_hash, name, bio, avatar_url, province, district, geohash, role, balance_hours, created_at, updated_at)
VALUES
(1, 'alice@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Alice Aydın', 'I am 42 years old and graduated from Boğaziçi University with a degree in Sociology.
 After working in a fast-paced office environment for a few years, I realized how disconnected I felt from people and from making things with my hands.
 I grew up learning small crafts and traditions from older family members and I miss that slower way of sharing time.
 On this platform, I wanto to offer calm, hands-on activities and quiet conversations.', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788309/alice_aydin_k7xtyo.jpg', 'Istanbul', 'Kadıköy', 'sxk9hqng', 'USER', 10, NOW(), NOW()),
(2, 'bob@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Bob Balcı', ' I have always been drawn to objects and stories that carry memory, whether that is old photographs, books, or collectible cards.
 I work full-time in an unrelated field, but learning remains central to my life. I am here to share interests that are usually considered unproductive or childish, because I believe curiosity has no age limit', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788311/bob_balci_kbzypk.jpg', 'Istanbul', 'Üsküdar', 'sxk9khms', 'USER', 10, NOW(), NOW()),
(3, 'ceren@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Ceren Çelik', 'I am 26 years old and graduated from Hacettepe University with a degree in Communication Sciences.
 Photography started as a hobby during university, but over time it became a way for me to observe the world more carefully. I am still learning and often feel unsure about my work, which is why I prefer shared practice rather than formal teaching.', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788309/ceren_kvrtfv.jpg', 'Ankara', 'Çankaya', 'sxp6gvtx', 'USER', 10, NOW(), NOW()),
(4, 'deniz@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Deniz Demir', 'I am 25 years old and graduated from Ege University with a degree in Environmental Engineering.
 Over the years, I developed a deep appreciation for nature and slow observation. I am not an expert, but I enjoy learning through being present and paying attention.', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788310/denizdemir_jcndve.jpg', 'Izmir', 'Karşıyaka', 'swg6bbp', 'USER', 10, NOW(), NOW()),
(5, 'meryem@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Meryem Erdoğan', ' For many years, my life was shaped family responsibilities, leaving little time for personal learning.
 Recently, I have been trying to reconnect with things I postponed, including physical movement and new skills. I value patience, encouragement, and people who understand that learning later in life comes with fear as well as excitement.', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788310/meryem_enysi1.jpg', 'Istanbul', 'Beşiktaş', 'sxk9', 'USER', 10, NOW(), NOW()),
(6, 'admin@example.com', '$2a$12$ghIyUZLjPNnQudmitgsimuViKIbP4Wo1Gi8eiVHrbL5yK.rt7N4pa', 'Admin User', 'Platform moderator', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788309/admin_ylcrd7.jpg', 'Istanbul', 'Beşiktaş', 'sxk9', 'ADMIN', 10, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- OFFERS
INSERT INTO offers (id, provider_id, title, description, duration_hours, start_date, end_date, province, district, geohash, image_urls, status, created_at, updated_at)
VALUES
(1, 1, 'Making and Flying a Kite Together', 'I grew up making kites with my grandfather using paper, wood sticks, and string. I would love to share this simple tradition with someone who enjoys slow, hands-on activities. We can build a kite together from scratch and, if the weather allows, go outside and fly it. No experience needed. Just curiosity and patience.', 3, '2025-12-05', '2026-01-15', 'Istanbul', 'Kadıköy', 'sxk9hqng', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788310/offer1_idqtm7.jpg', 'ACTIVE', NOW(), NOW()),
(2, 2, 'Showing My Pokémon Card Collection', 'I have been collecting Pokémon cards since childhood and still keep them carefully organized. This is not about buying or trading. I simply enjoy sharing the stories behind the cards, how I found them, and why some of them matter to me emotionally. Happy to meet someone who is curious, nostalgic, or just wants to listen.', 1, '2025-12-10', '2026-01-25', 'Istanbul', 'Üsküdar', 'sxk9khms', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788311/offer2_a7kzxc.webp', 'ACTIVE', NOW(), NOW()),
(3, 3, 'Street Photography Walk With Honest Feedback', 'I am trying to improve my street photography by spending more time outside and talking through the process. We can walk together, observe people and light, take photos, and discuss what works and what doesnt. This is about learning together, not teaching from above.', 2, '2025-12-01', '2026-01-31', 'Ankara', 'Çankaya', 'sxp6gvtx', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788310/offer3_hjinzr.jpg', 'ACTIVE', NOW(), NOW()),
(4, 4, 'Slow Bird Watching for Beginners', 'I enjoy spending quiet mornings observing birds and learning their patterns. I am not an expert, but I can help identify common species and share how I learned to notice them. The walk is slow and calm, focused on paying attention rather than reaching a destination.', 2, '2025-12-08', '2026-01-20', 'Izmir', 'Karşıyaka', 'swg6bbp', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788314/offer4_p3f4t6.jpg', 'ACTIVE', NOW(), NOW()),
(5, 2, 'Mushroom Picking Walk in Belgrad Forest', 'I regularly visit Belgrad Forest and have learned how to identify a few safe, edible mushrooms. I want to share this knowledge carefully and responsibly, emphasizing safety and respect for nature. This is a learning walk, not a harvesting mission.', 3, '2025-12-15', '2026-02-01', 'Istanbul', 'Sarıyer', 'sxkdkdk', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788314/offer5_nvdjc2.jpg', 'ACTIVE', NOW(), NOW()),
(6, 5, 'Learning Basic Sewing to Repair Everyday Clothes', 'I know simple sewing techniques that have helped me keep clothes longer instead of throwing them away. I can show how to sew buttons, close small tears, and make basic repairs by hand. This is slow, practical, and beginner-friendly.', 2, '2025-12-03', '2026-01-10', 'Istanbul', 'Kadıköy', 'sxk9hqnf', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788315/offer6_itutep.jpg', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- REQUESTS
-- NOTE: Request IDs start at 1000000 to ensure no conflicts with offer IDs
INSERT INTO requests (id, seeker_id, title, description, duration_hours, start_date, end_date, province, district, geohash, image_urls, status, created_at, updated_at)
VALUES
(1000000, 3, 'Looking for Gentle English Conversation', 'I can read and understand English, but speaking makes me nervous. I am looking for someone patient who is okay with pauses, mistakes, and thinking out loud. The goal is confidence, not perfection.', 1, '2025-12-12', '2026-01-30', 'Ankara', 'Çankaya', 'sxp6gvtx', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788315/request1_byjj4l.jpg', 'ACTIVE', NOW(), NOW()),
(1000001, 4, 'Want to Learn How to Make Kısır Properly', 'I have tried to make kısır several times, but it never tastes right. I want to learn the small details that recipes do not explain: texture, timing, and balance. I prefer learning by doing, side by side.', 2, '2025-12-06', '2026-01-18', 'Izmir', 'Karşıyaka', 'swg6bbp', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788317/request2_dwyaty.jpg', 'ACTIVE', NOW(), NOW()),
(1000002, 1, 'Need Support With High School Mathematics', 'I am a mother of a high school student and I want to better support my son with his mathematics classes. Math was never easy for me, and I still feel hesitant asking questions, even now.
  I am not looking for someone to replace a teacher, but for calm, judgment-free help so I can understand the topics well enough to explain them at home.', 2, '2025-12-01', '2026-01-28', 'Istanbul', 'Kadıköy', 'sxk9hqnf', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788318/request3_za7yb0.jpg', 'ACTIVE', NOW(), NOW()),
(1000003, 5, 'Learning How to Ride a Bicycle Later in Life', 'I never learned how to ride a bicycle when I was younger. Now that I am older, I want to try slowly and safely. I am looking for someone patient who understands fear and takes things step by step.', 2, '2025-12-20', '2026-02-01', 'Istanbul', 'Beşiktaş', 'sxk9', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765788318/request4_gerb9d.jpg', 'ACTIVE', NOW(), NOW()),
(1000004, 2, 'Help Teaching My Dog Basic Commands', 'I recently adopted a dog and we are still learning each other. I want help teaching simple commands like sit, stay, and walking calmly. I am more interested in building trust than strict training.', 1, '2025-12-10', '2026-01-22', 'Istanbul', 'Üsküdar', 'sxk9khms', 'https://res.cloudinary.com/dcsagh0da/image/upload/v1765797858/request5_crop_uoiqsb.jpg', 'ACTIVE', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- SEMANTIC TAGS
INSERT INTO semantic_tags (id, name, wikidata_id)
VALUES
(1, 'Crafts', 'Q2207288'),
(2, 'Kites', 'Q1313642'),
(3, 'Collecting', 'Q2046089'),
(4, 'Pokémon', 'Q5420'),
(5, 'Photography', 'Q11633'),
(6, 'Street Photography', 'Q1970746'),
(7, 'Bird Watching', 'Q189673'),
(8, 'Nature', 'Q7860'),
(9, 'Foraging', 'Q1438016'),
(10, 'Mushrooms', 'Q83093'),
(11, 'Sewing', 'Q193213'),
(12, 'Repair', 'Q43015'),
(13, 'English Language', 'Q1860'),
(14, 'Language Learning', 'Q2288963'),
(15, 'Cooking', 'Q38695'),
(16, 'Turkish Cuisine', 'Q846963'),
(17, 'Mathematics', 'Q395'),
(18, 'Education', 'Q8434'),
(19, 'Cycling', 'Q53121'),
(20, 'Sports', 'Q349'),
(21, 'Pets', 'Q144'),
(22, 'Dog Training', 'Q1229765')
ON CONFLICT (id) DO NOTHING;

-- TAG LINKS
-- Offer 1: Kite making
INSERT INTO offer_tags (offer_id, tag_id) VALUES (1, 1) ON CONFLICT DO NOTHING; -- Crafts
INSERT INTO offer_tags (offer_id, tag_id) VALUES (1, 2) ON CONFLICT DO NOTHING; -- Kites

-- Offer 2: Pokémon cards
INSERT INTO offer_tags (offer_id, tag_id) VALUES (2, 3) ON CONFLICT DO NOTHING; -- Collecting
INSERT INTO offer_tags (offer_id, tag_id) VALUES (2, 4) ON CONFLICT DO NOTHING; -- Pokémon

-- Offer 3: Street photography
INSERT INTO offer_tags (offer_id, tag_id) VALUES (3, 5) ON CONFLICT DO NOTHING; -- Photography
INSERT INTO offer_tags (offer_id, tag_id) VALUES (3, 6) ON CONFLICT DO NOTHING; -- Street Photography

-- Offer 4: Bird watching
INSERT INTO offer_tags (offer_id, tag_id) VALUES (4, 7) ON CONFLICT DO NOTHING; -- Bird Watching
INSERT INTO offer_tags (offer_id, tag_id) VALUES (4, 8) ON CONFLICT DO NOTHING; -- Nature

-- Offer 5: Mushroom picking
INSERT INTO offer_tags (offer_id, tag_id) VALUES (5, 9) ON CONFLICT DO NOTHING; -- Foraging
INSERT INTO offer_tags (offer_id, tag_id) VALUES (5, 10) ON CONFLICT DO NOTHING; -- Mushrooms
INSERT INTO offer_tags (offer_id, tag_id) VALUES (5, 8) ON CONFLICT DO NOTHING; -- Nature

-- Offer 6: Sewing
INSERT INTO offer_tags (offer_id, tag_id) VALUES (6, 11) ON CONFLICT DO NOTHING; -- Sewing
INSERT INTO offer_tags (offer_id, tag_id) VALUES (6, 12) ON CONFLICT DO NOTHING; -- Repair

-- Request 1000000: English conversation
INSERT INTO request_tags (request_id, tag_id) VALUES (1000000, 13) ON CONFLICT DO NOTHING; -- English Language
INSERT INTO request_tags (request_id, tag_id) VALUES (1000000, 14) ON CONFLICT DO NOTHING; -- Language Learning

-- Request 1000001: Kısır
INSERT INTO request_tags (request_id, tag_id) VALUES (1000001, 15) ON CONFLICT DO NOTHING; -- Cooking
INSERT INTO request_tags (request_id, tag_id) VALUES (1000001, 16) ON CONFLICT DO NOTHING; -- Turkish Cuisine

-- Request 1000002: Math tutoring
INSERT INTO request_tags (request_id, tag_id) VALUES (1000002, 17) ON CONFLICT DO NOTHING; -- Mathematics
INSERT INTO request_tags (request_id, tag_id) VALUES (1000002, 18) ON CONFLICT DO NOTHING; -- Education

-- Request 1000003: Bicycle learning
INSERT INTO request_tags (request_id, tag_id) VALUES (1000003, 19) ON CONFLICT DO NOTHING; -- Cycling
INSERT INTO request_tags (request_id, tag_id) VALUES (1000003, 20) ON CONFLICT DO NOTHING; -- Sports

-- Request 1000004: Dog training
INSERT INTO request_tags (request_id, tag_id) VALUES (1000004, 21) ON CONFLICT DO NOTHING; -- Pets
INSERT INTO request_tags (request_id, tag_id) VALUES (1000004, 22) ON CONFLICT DO NOTHING; -- Dog Training

-- HANDSHAKES
INSERT INTO handshakes (id, offer_id, request_id, seeker_id, provider_id, status, duration_hours, created_at, agreed_at)
VALUES
(1, 1, NULL, 3, 1, 'COMPLETED', 3, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days'),
(2, NULL, 1000000, 3, 5, 'COMPLETED', 1, NOW() - INTERVAL '5 days', NOW() - INTERVAL '4 days'),
(3, 3, NULL, 1, 3, 'COMPLETED', 2, NOW() - INTERVAL '8 days', NOW() - INTERVAL '7 days'),
(4, 4, NULL, 2, 4, 'COMPLETED', 2, NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days'),
(5, 5, NULL, 1, 2, 'COMPLETED', 3, NOW() - INTERVAL '9 days', NOW() - INTERVAL '8 days'),
(6, 6, NULL, 4, 1, 'COMPLETED', 2, NOW() - INTERVAL '4 days', NOW() - INTERVAL '3 days'),
(7, NULL, 1000001, 4, 2, 'COMPLETED', 2, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days'),
(8, NULL, 1000002, 1, 3, 'COMPLETED', 2, NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days'),
(9, NULL, 1000003, 5, 4, 'COMPLETED', 2, NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days'),
(10, NULL, 1000004, 2, 1, 'COMPLETED', 1, NOW() - INTERVAL '6 days', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- TIMEBANK TRANSACTIONS
INSERT INTO timebank_transactions (id, sender_id, receiver_id, handshake_id, amount, description, created_at)
VALUES
(1, 3, 1, 1, 3, 'Time exchange for kite making', NOW() - INTERVAL '6 days'),
(2, 5, 3, 2, 1, 'Time exchange for English conversation', NOW() - INTERVAL '4 days'),
(3, 1, 3, 3, 2, 'Time exchange for street photography walk', NOW() - INTERVAL '7 days'),
(4, 2, 4, 4, 2, 'Time exchange for bird watching', NOW() - INTERVAL '5 days'),
(5, 1, 2, 5, 3, 'Time exchange for mushroom picking walk', NOW() - INTERVAL '8 days'),
(6, 4, 1, 6, 2, 'Time exchange for sewing basics', NOW() - INTERVAL '3 days'),
(7, 2, 4, 7, 2, 'Time exchange for learning kısır', NOW() - INTERVAL '6 days'),
(8, 3, 1, 8, 2, 'Time exchange for math tutoring', NOW() - INTERVAL '9 days'),
(9, 4, 5, 9, 2, 'Time exchange for bicycle learning', NOW() - INTERVAL '2 days'),
(10, 1, 2, 10, 1, 'Time exchange for dog training help', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- RATINGS
INSERT INTO ratings (id, handshake_id, rater_id, ratee_id, punctuality, friendliness, communicative, preparedness, comment, created_at)
VALUES
(1, 1, 3, 1, 4, 5, 4, 3, 'The kite-making session felt very warm and personal. We took our time, and I enjoyed hearing the story behind it. We did struggle a bit with materials, but that was part of the experience. I left feeling calmer than when I arrived.', NOW() - INTERVAL '6 days'),
(2, 2, 3, 5, 5, 5, 5, 3, 'I felt safe making mistakes and taking pauses. Sometimes we lost structure, but honestly that helped my confidence. This was more about comfort than language rules, and that worked for me.', NOW() - INTERVAL '4 days'),
(3, 3, 1, 3, 5, 4, 4, 4, 'The walk was inspiring and I appreciated the honest feedback. At times I wished for a bit more direction, but the shared learning approach made sense. I would do this again with clearer goals next time.', NOW() - INTERVAL '7 days'),
(4, 4, 2, 4, 5, 5, 4, 5, 'This was exactly what I needed. The pace was slow, explanations were gentle, and silence was respected. I learned without feeling rushed or overwhelmed.', NOW() - INTERVAL '5 days'),
(5, 5, 1, 2, 4, 4, 3, 3, 'The walk itself was enjoyable, but I felt a bit unsure about identifying mushrooms. I would have appreciated clearer safety boundaries. Still, I value the intention and the respect for nature.', NOW() - INTERVAL '8 days'),
(6, 6, 4, 1, 5, 5, 4, 5, 'I finally fixed clothes I had avoided for months. The explanations were slow and clear, without making me feel incompetent. It felt empowering in a very quiet way.', NOW() - INTERVAL '3 days'),
(7, 7, 4, 2, 4, 5, 4, 3, 'I learned things you never find in recipes. The process was a bit messy, but that made it feel real. I now understand what I was doing wrong before.', NOW() - INTERVAL '6 days'),
(8, 8, 1, 3, 5, 4, 3, 4, 'They were patient and never made me feel stupid. Sometimes explanations moved a bit fast for me, but when I asked to slow down, they adjusted. That mattered more than perfect clarity.', NOW() - INTERVAL '9 days'),
(9, 9, 5, 4, 5, 5, 5, 4, 'I was scared at first and embarrassed about my age. That fear was handled with respect and patience. I didn''t fully learn to ride yet, but I now believe I can.', NOW() - INTERVAL '2 days'),
(10, 10, 2, 1, 4, 4, 3, 3, 'The session helped, but I realized I needed more consistency than a single meeting. The approach was kind, though sometimes unclear. Still a positive step for me and my dog.', NOW() - INTERVAL '5 days')
ON CONFLICT (id) DO NOTHING;

-- BADGES
INSERT INTO badges (id, name, description, icon_url)
VALUES
(1, 'Newcomer', 'Awarded for joining the community.', '🌱'),
(2, 'Community Helper', 'Awarded after completing 3 exchanges.', '🤝'),
(3, 'Active Member', 'Awarded after completing 10 exchanges.', '⭐'),
(4, 'Veteran', 'Awarded after completing 25 exchanges.', '🏅'),
(5, 'Champion', 'Awarded after completing 50 exchanges.', '🏆')
ON CONFLICT (id) DO NOTHING;

-- USER BADGES (Only one badge per user - their highest earned)
INSERT INTO user_badges (user_id, badge_id, earned_at)
VALUES
(1, 2, NOW()),   -- Alice - Community Helper (has 4 completed exchanges)
(2, 2, NOW()),   -- Bob - Community Helper (has 3 completed exchanges)
(3, 2, NOW()),   -- Ceren - Community Helper (has 3 completed exchanges)
(4, 2, NOW()),   -- Deniz - Community Helper (has 4 completed exchanges)
(5, 1, NOW())    -- Elif - Newcomer (has 1 completed exchange)
ON CONFLICT DO NOTHING;

-- FORUM TOPICS
INSERT INTO forum_topics (id, author_id, title, created_at, updated_at)
VALUES
(1, 2, 'Welcome to The Hive!', NOW(), NOW()),
(2, 1, 'Sharing Skills Without Pressure', NOW(), NOW()),
(3, 6, 'Platform Updates & Feedback', NOW(), NOW()),
(4, 4, 'Slow Activities and Learning', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- FORUM POSTS
INSERT INTO forum_posts (id, topic_id, author_id, content, created_at)
VALUES
(1, 1, 1, 'Happy to be part of this community!', NOW()),
(2, 1, 2, 'Glad to have you here, Alice!', NOW()),
(3, 2, 3, 'I appreciate that this platform values learning together over teaching from above.', NOW()),
(4, 2, 1, 'Agreed. The exchanges feel more honest this way.', NOW()),
(5, 3, 6, 'We''ve rolled out minor bug fixes and performance improvements.', NOW()),
(6, 3, 4, 'App feels smoother now, nice work!', NOW()),
(7, 4, 4, 'Does anyone else value slow, patient activities? I find them grounding.', NOW()),
(8, 4, 1, 'Absolutely. The kite-making session was one of the calmest experiences I''ve had.', NOW()),
(9, 4, 5, 'I''m new but already feel welcomed. Thank you all.', NOW())
ON CONFLICT (id) DO NOTHING;

-- QUESTIONS
INSERT INTO questions (id, offer_id, request_id, asker_id, content, created_at)
VALUES
(1, 1, NULL, 3, 'I''ve never made a kite before. Is this really for beginners?', NOW() - INTERVAL '8 days'),
(2, 2, NULL, 3, 'I used to collect Pokémon cards as a child. Can I share my own stories too?', NOW() - INTERVAL '6 days'),
(3, 3, NULL, 1, 'I''m nervous about street photography. Will this help me get over that fear?', NOW() - INTERVAL '9 days'),
(4, 4, NULL, 2, 'What time of day is best for bird watching?', NOW() - INTERVAL '7 days'),
(5, 5, NULL, 1, 'Is it safe to eat mushrooms we find? I''m concerned about this.', NOW() - INTERVAL '10 days'),
(6, 6, NULL, 4, 'Do I need to bring my own materials for the sewing session?', NOW() - INTERVAL '5 days'),
(7, NULL, 1000000, 5, 'I can help with English conversation. Would you be interested in meeting weekly?', NOW() - INTERVAL '6 days'),
(8, NULL, 1000002, 3, 'I''ve tutored high school math before. What topics are you struggling with most?', NOW() - INTERVAL '11 days')
ON CONFLICT (id) DO NOTHING;

-- ANSWERS
INSERT INTO answers (id, question_id, responder_id, content, created_at)
VALUES
(1, 1, 1, 'Yes, absolutely. No experience needed. We''ll take our time and build it step by step.', NOW() - INTERVAL '8 days' + INTERVAL '2 hours'),
(2, 2, 2, 'Of course! I would love that. It''s more meaningful when stories are shared, not just shown.', NOW() - INTERVAL '6 days' + INTERVAL '1 hour'),
(3, 3, 3, 'I''m nervous too, honestly. That''s why I want to practice with someone else. We can support each other.', NOW() - INTERVAL '9 days' + INTERVAL '3 hours'),
(4, 4, 4, 'Early mornings are best, especially just after sunrise. Birds are most active then.', NOW() - INTERVAL '7 days' + INTERVAL '2 hours'),
(5, 5, 2, 'Safety is my priority. I only teach about mushrooms I''m 100% confident identifying, and we won''t harvest anything risky.', NOW() - INTERVAL '10 days' + INTERVAL '4 hours'),
(6, 6, 1, 'I have basic materials, but if you have clothes you want to repair, bring them along.', NOW() - INTERVAL '5 days' + INTERVAL '1 hour'),
(7, 7, 3, 'That sounds perfect. Weekly would help me build consistency. Thank you.', NOW() - INTERVAL '6 days' + INTERVAL '3 hours'),
(8, 8, 1, 'Algebra mostly. I struggle with word problems and visualizing equations.', NOW() - INTERVAL '11 days' + INTERVAL '2 hours')
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
SELECT setval('questions_id_seq', (SELECT MAX(id) FROM questions));
SELECT setval('answers_id_seq', (SELECT MAX(id) FROM answers));
