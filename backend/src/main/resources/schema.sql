-- Drop existing types if they exist
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS item_status CASCADE;
DROP TYPE IF EXISTS handshake_status CASCADE;
DROP TYPE IF EXISTS report_status CASCADE;

-- Create ENUM types
CREATE TYPE user_role AS ENUM ('USER', 'ADMIN');
CREATE TYPE item_status AS ENUM ('ACTIVE', 'EXPIRED', 'ARCHIVED');
CREATE TYPE handshake_status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED');
CREATE TYPE report_status AS ENUM ('OPEN', 'IN_REVIEW', 'RESOLVED');

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    bio TEXT,
    province VARCHAR(100),
    district VARCHAR(100),
    geohash VARCHAR(20),
    role user_role DEFAULT 'USER',
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
    status item_status DEFAULT 'ACTIVE',
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
    status item_status DEFAULT 'ACTIVE',
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
    offer_id INT NOT NULL,
    seeker_id INT NOT NULL,
    provider_id INT NOT NULL,
    status handshake_status DEFAULT 'PENDING',
    agreed_hours INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
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
    message TEXT,
    status report_status DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
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

-- Add foreign key constraints
ALTER TABLE offers ADD CONSTRAINT fk_offers_provider FOREIGN KEY (provider_id) REFERENCES users(id);
ALTER TABLE requests ADD CONSTRAINT fk_requests_seeker FOREIGN KEY (seeker_id) REFERENCES users(id);
ALTER TABLE offer_tags ADD CONSTRAINT fk_offer_tags_offer FOREIGN KEY (offer_id) REFERENCES offers(id) ON DELETE CASCADE;
ALTER TABLE offer_tags ADD CONSTRAINT fk_offer_tags_tag FOREIGN KEY (tag_id) REFERENCES semantic_tags(id) ON DELETE CASCADE;
ALTER TABLE request_tags ADD CONSTRAINT fk_request_tags_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE;
ALTER TABLE request_tags ADD CONSTRAINT fk_request_tags_tag FOREIGN KEY (tag_id) REFERENCES semantic_tags(id) ON DELETE CASCADE;
ALTER TABLE handshakes ADD CONSTRAINT fk_handshakes_offer FOREIGN KEY (offer_id) REFERENCES offers(id);
ALTER TABLE handshakes ADD CONSTRAINT fk_handshakes_seeker FOREIGN KEY (seeker_id) REFERENCES users(id);
ALTER TABLE handshakes ADD CONSTRAINT fk_handshakes_provider FOREIGN KEY (provider_id) REFERENCES users(id);
ALTER TABLE timebank_transactions ADD CONSTRAINT fk_transactions_sender FOREIGN KEY (sender_id) REFERENCES users(id);
ALTER TABLE timebank_transactions ADD CONSTRAINT fk_transactions_receiver FOREIGN KEY (receiver_id) REFERENCES users(id);
ALTER TABLE timebank_transactions ADD CONSTRAINT fk_transactions_handshake FOREIGN KEY (handshake_id) REFERENCES handshakes(id);
ALTER TABLE ratings ADD CONSTRAINT fk_ratings_handshake FOREIGN KEY (handshake_id) REFERENCES handshakes(id);
ALTER TABLE ratings ADD CONSTRAINT fk_ratings_rater FOREIGN KEY (rater_id) REFERENCES users(id);
ALTER TABLE ratings ADD CONSTRAINT fk_ratings_ratee FOREIGN KEY (ratee_id) REFERENCES users(id);
ALTER TABLE questions ADD CONSTRAINT fk_questions_offer FOREIGN KEY (offer_id) REFERENCES offers(id);
ALTER TABLE questions ADD CONSTRAINT fk_questions_request FOREIGN KEY (request_id) REFERENCES requests(id);
ALTER TABLE questions ADD CONSTRAINT fk_questions_asker FOREIGN KEY (asker_id) REFERENCES users(id);
ALTER TABLE answers ADD CONSTRAINT fk_answers_question FOREIGN KEY (question_id) REFERENCES questions(id);
ALTER TABLE answers ADD CONSTRAINT fk_answers_responder FOREIGN KEY (responder_id) REFERENCES users(id);
ALTER TABLE user_badges ADD CONSTRAINT fk_user_badges_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_badges ADD CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE;
ALTER TABLE reports ADD CONSTRAINT fk_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id);
ALTER TABLE reports ADD CONSTRAINT fk_reports_reported FOREIGN KEY (reported_user_id) REFERENCES users(id);
ALTER TABLE messages ADD CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id);
ALTER TABLE messages ADD CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id);
ALTER TABLE messages ADD CONSTRAINT fk_messages_offer FOREIGN KEY (offer_id) REFERENCES offers(id);
ALTER TABLE messages ADD CONSTRAINT fk_messages_request FOREIGN KEY (request_id) REFERENCES requests(id);
ALTER TABLE messages ADD CONSTRAINT fk_messages_handshake FOREIGN KEY (handshake_id) REFERENCES handshakes(id);
ALTER TABLE forum_topics ADD CONSTRAINT fk_forum_topics_author FOREIGN KEY (author_id) REFERENCES users(id);
ALTER TABLE forum_posts ADD CONSTRAINT fk_forum_posts_topic FOREIGN KEY (topic_id) REFERENCES forum_topics(id) ON DELETE CASCADE;
ALTER TABLE forum_posts ADD CONSTRAINT fk_forum_posts_author FOREIGN KEY (author_id) REFERENCES users(id);


