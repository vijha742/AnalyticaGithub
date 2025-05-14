-- GitHub Users table
CREATE TABLE github_users (
    id VARCHAR(255) PRIMARY KEY,
    github_username VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    email VARCHAR(255),
    avatar_url TEXT,
    bio TEXT,
    followers_count INTEGER,
    following_count INTEGER,
    public_repos_count INTEGER,
    total_contributions INTEGER,
    repositories JSONB,  -- Stores repository list as JSON
    contributions JSONB, -- Stores contribution list as JSON
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT github_users_username_unique UNIQUE (github_username)
);

-- Rate Limits table to track GitHub API rate limits
CREATE TABLE rate_limits (
    id SERIAL PRIMARY KEY,
    limit_count INTEGER NOT NULL,
    remaining INTEGER NOT NULL,
    reset_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Suggested Users table
CREATE TABLE suggested_users (
    id BIGSERIAL PRIMARY KEY,
    github_username VARCHAR(255) NOT NULL UNIQUE,
    suggested_by VARCHAR(255) NOT NULL,
    suggested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- GitHub User Data
    name VARCHAR(255),
    email VARCHAR(255),
    avatar_url TEXT,
    bio TEXT,
    followers_count INTEGER,
    following_count INTEGER,
    public_repos_count INTEGER,
    total_contributions INTEGER,
    repositories JSONB,  -- Stores repository list as JSON
    contributions JSONB, -- Stores contribution list as JSON
    last_refreshed TIMESTAMP WITH TIME ZONE,
    
    CONSTRAINT suggested_users_username_unique UNIQUE (github_username)
);

-- User Contributions Time Series
CREATE TABLE contribution_history (
    id BIGSERIAL PRIMARY KEY,
    github_username VARCHAR(255) NOT NULL,
    date DATE NOT NULL,
    commit_count INTEGER DEFAULT 0,
    pr_count INTEGER DEFAULT 0,
    issue_count INTEGER DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    total_count INTEGER DEFAULT 0,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT contribution_history_username_date_unique UNIQUE (github_username, date),
    CONSTRAINT contribution_history_username_fk FOREIGN KEY (github_username) REFERENCES suggested_users(github_username) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_github_users_username ON github_users(github_username);
CREATE INDEX idx_suggested_users_username ON suggested_users(github_username);
CREATE INDEX idx_suggested_users_active ON suggested_users(is_active);
CREATE INDEX idx_contribution_history_username ON contribution_history(github_username);
CREATE INDEX idx_contribution_history_date ON contribution_history(date);

-- Create GIN indexes for JSON fields to enable efficient querying of repositories and contributions
CREATE INDEX idx_github_users_repositories ON github_users USING GIN (repositories);
CREATE INDEX idx_github_users_contributions ON github_users USING GIN (contributions);
CREATE INDEX idx_suggested_users_repositories ON suggested_users USING GIN (repositories);
CREATE INDEX idx_suggested_users_contributions ON suggested_users USING GIN (contributions);