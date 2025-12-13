-- Note: Using VARCHAR for enum types for better JPA/Hibernate compatibility

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    bio TEXT,
    avatar_url TEXT,
    province VARCHAR(100),
    district VARCHAR(100),
    geohash VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER',
    account_status VARCHAR(20) DEFAULT 'ACTIVE',
    warning_count INT DEFAULT 0,
    balance_hours INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Offers table
CREATE TABLE IF NOT EXISTS offers (
    id SERIAL PRIMARY KEY,
    provider_id INT NOT NULL,
    title VARCHAR(255),
    description TEXT,
    duration_hours INT,
    start_date DATE,
    end_date DATE,
    province VARCHAR(100),
    district VARCHAR(100),
    geohash VARCHAR(20),
    image_urls TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Requests table
CREATE TABLE IF NOT EXISTS requests (
    id SERIAL PRIMARY KEY,
    seeker_id INT NOT NULL,
    title VARCHAR(255),
    description TEXT,
    duration_hours INT,
    start_date DATE,
    end_date DATE,
    province VARCHAR(100),
    district VARCHAR(100),
    geohash VARCHAR(20),
    image_urls TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Semantic tags table
CREATE TABLE IF NOT EXISTS semantic_tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    wikidata_id VARCHAR(50)
);

-- Offer tags table (junction table)
CREATE TABLE IF NOT EXISTS offer_tags (
    offer_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (offer_id, tag_id)
);

-- Request tags table (junction table)
CREATE TABLE IF NOT EXISTS request_tags (
    request_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (request_id, tag_id)
);

-- Handshakes table
CREATE TABLE IF NOT EXISTS handshakes (
    id SERIAL PRIMARY KEY,
    offer_id INT,
    request_id INT,
    seeker_id INT NOT NULL,
    provider_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    duration_hours INT,
    seeker_confirmed BOOLEAN DEFAULT FALSE,
    provider_confirmed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    agreed_at TIMESTAMP,
    CONSTRAINT chk_offer_or_request CHECK (
        (offer_id IS NOT NULL AND request_id IS NULL) OR
        (offer_id IS NULL AND request_id IS NOT NULL)
    ),
    CONSTRAINT unique_offer_seeker UNIQUE (offer_id, seeker_id),
    CONSTRAINT unique_request_seeker UNIQUE (request_id, seeker_id)
);
-- Timebank transactions table
CREATE TABLE IF NOT EXISTS timebank_transactions (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    handshake_id INT NOT NULL,
    amount INT,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ratings table
CREATE TABLE IF NOT EXISTS ratings (
    id SERIAL PRIMARY KEY,
    handshake_id INT NOT NULL,
    rater_id INT NOT NULL,
    ratee_id INT NOT NULL,
    punctuality INT CHECK (punctuality BETWEEN 1 AND 5),
    friendliness INT CHECK (friendliness BETWEEN 1 AND 5),
    communicative INT CHECK (communicative BETWEEN 1 AND 5),
    preparedness INT CHECK (preparedness BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Questions table
CREATE TABLE IF NOT EXISTS questions (
    id SERIAL PRIMARY KEY,
    offer_id INT,
    request_id INT,
    asker_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Answers table
CREATE TABLE IF NOT EXISTS answers (
    id SERIAL PRIMARY KEY,
    question_id INT UNIQUE NOT NULL,
    responder_id INT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Badges table
CREATE TABLE IF NOT EXISTS badges (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    description TEXT,
    icon_url VARCHAR(255)
);

-- User badges table (junction table)
CREATE TABLE IF NOT EXISTS user_badges (
    user_id INT NOT NULL,
    badge_id INT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, badge_id)
);

-- Reports table
CREATE TABLE IF NOT EXISTS reports (
    id SERIAL PRIMARY KEY,
    reporter_id INT NOT NULL,
    reported_user_id INT NOT NULL,
    report_type VARCHAR(20) DEFAULT 'USER',
    reported_offer_id INT,
    reported_request_id INT,
    reported_forum_post_id INT,
    reported_forum_topic_id INT,
    message TEXT,
    admin_notes TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by_id INT
);

-- User actions table
CREATE TABLE IF NOT EXISTS user_actions (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    admin_id INT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    reason TEXT,
    report_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id SERIAL PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT,
    offer_id INT,
    request_id INT,
    handshake_id INT,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Forum topics table
CREATE TABLE IF NOT EXISTS forum_topics (
    id SERIAL PRIMARY KEY,
    author_id INT NOT NULL,
    title VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Forum posts table
CREATE TABLE IF NOT EXISTS forum_posts (
    id SERIAL PRIMARY KEY,
    topic_id INT NOT NULL,
    author_id INT NOT NULL,
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraints (only if they don't exist)
DO $$
BEGIN
    -- Offers constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_offers_provider') THEN
        ALTER TABLE offers ADD CONSTRAINT fk_offers_provider FOREIGN KEY (provider_id) REFERENCES users(id);
    END IF;
    
    -- Requests constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_requests_seeker') THEN
        ALTER TABLE requests ADD CONSTRAINT fk_requests_seeker FOREIGN KEY (seeker_id) REFERENCES users(id);
    END IF;
    
    -- Offer tags constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_offer_tags_offer') THEN
        ALTER TABLE offer_tags ADD CONSTRAINT fk_offer_tags_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_offer_tags_tag') THEN
        ALTER TABLE offer_tags ADD CONSTRAINT fk_offer_tags_tag FOREIGN KEY (tag_id) REFERENCES semantic_tags(id) ON DELETE CASCADE;
    END IF;
    
    -- Request tags constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_request_tags_request') THEN
        ALTER TABLE request_tags ADD CONSTRAINT fk_request_tags_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_request_tags_tag') THEN
        ALTER TABLE request_tags ADD CONSTRAINT fk_request_tags_tag FOREIGN KEY (tag_id) REFERENCES semantic_tags(id) ON DELETE CASCADE;
    END IF;
    
    -- Handshakes constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_handshakes_offer') THEN
        ALTER TABLE handshakes ADD CONSTRAINT fk_handshakes_offer FOREIGN KEY (offer_id) REFERENCES offers(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_handshakes_seeker') THEN
        ALTER TABLE handshakes ADD CONSTRAINT fk_handshakes_seeker FOREIGN KEY (seeker_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_handshakes_provider') THEN
        ALTER TABLE handshakes ADD CONSTRAINT fk_handshakes_provider FOREIGN KEY (provider_id) REFERENCES users(id);
    END IF;
    
    -- Timebank transactions constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_transactions_sender') THEN
        ALTER TABLE timebank_transactions ADD CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_transactions_receiver') THEN
        ALTER TABLE timebank_transactions ADD CONSTRAINT fk_transactions_receiver FOREIGN KEY (receiver_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_transactions_handshake') THEN
        ALTER TABLE timebank_transactions ADD CONSTRAINT fk_transactions_handshake FOREIGN KEY (handshake_id) REFERENCES handshakes(id);
    END IF;
    
    -- Ratings constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_ratings_handshake') THEN
        ALTER TABLE ratings ADD CONSTRAINT fk_ratings_handshake FOREIGN KEY (handshake_id) REFERENCES handshakes(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_ratings_rater') THEN
        ALTER TABLE ratings ADD CONSTRAINT fk_ratings_rater FOREIGN KEY (rater_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_ratings_ratee') THEN
        ALTER TABLE ratings ADD CONSTRAINT fk_ratings_ratee FOREIGN KEY (ratee_id) REFERENCES users(id);
    END IF;
    
    -- Questions constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_questions_offer') THEN
        ALTER TABLE questions ADD CONSTRAINT fk_questions_offer FOREIGN KEY (offer_id) REFERENCES offers(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_questions_request') THEN
        ALTER TABLE questions ADD CONSTRAINT fk_questions_request FOREIGN KEY (request_id) REFERENCES requests(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_questions_asker') THEN
        ALTER TABLE questions ADD CONSTRAINT fk_questions_asker FOREIGN KEY (asker_id) REFERENCES users(id);
    END IF;
    
    -- Answers constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_answers_question') THEN
        ALTER TABLE answers ADD CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_answers_responder') THEN
        ALTER TABLE answers ADD CONSTRAINT fk_answers_responder FOREIGN KEY (responder_id) REFERENCES users(id);
    END IF;
    
    -- User badges constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_badges_user') THEN
        ALTER TABLE user_badges ADD CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_badges_badge') THEN
        ALTER TABLE user_badges ADD CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE;
    END IF;
    
    -- Reports constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_reporter') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_reported') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_reported FOREIGN KEY (reported_user_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_offer') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_offer FOREIGN KEY (reported_offer_id) REFERENCES offers(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_request') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_request FOREIGN KEY (reported_request_id) REFERENCES requests(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_forum_post') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_forum_post FOREIGN KEY (reported_forum_post_id) REFERENCES forum_posts(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_forum_topic') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_forum_topic FOREIGN KEY (reported_forum_topic_id) REFERENCES forum_topics(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_reports_resolved_by') THEN
        ALTER TABLE reports ADD CONSTRAINT fk_reports_resolved_by FOREIGN KEY (resolved_by_id) REFERENCES users(id);
    END IF;
    
    -- User actions constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_actions_user') THEN
        ALTER TABLE user_actions ADD CONSTRAINT fk_user_actions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_actions_admin') THEN
        ALTER TABLE user_actions ADD CONSTRAINT fk_user_actions_admin FOREIGN KEY (admin_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_actions_report') THEN
        ALTER TABLE user_actions ADD CONSTRAINT fk_user_actions_report FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE SET NULL;
    END IF;
    
    -- Messages constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_sender') THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_receiver') THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_offer') THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_offer FOREIGN KEY (offer_id) REFERENCES offers(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_request') THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_request FOREIGN KEY (request_id) REFERENCES requests(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_handshake') THEN
        ALTER TABLE messages ADD CONSTRAINT fk_messages_handshake FOREIGN KEY (handshake_id) REFERENCES handshakes(id);
    END IF;
    
    -- Forum constraints
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_forum_topics_author') THEN
        ALTER TABLE forum_topics ADD CONSTRAINT fk_forum_topics_author FOREIGN KEY (author_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_forum_posts_topic') THEN
        ALTER TABLE forum_posts ADD CONSTRAINT fk_forum_posts_topic FOREIGN KEY (topic_id) REFERENCES forum_topics(id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_forum_posts_author') THEN
        ALTER TABLE forum_posts ADD CONSTRAINT fk_forum_posts_author FOREIGN KEY (author_id) REFERENCES users(id);
    END IF;
END $$;


