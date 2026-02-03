CREATE TABLE IF NOT EXISTS posts (
    id BIGSERIAL PRIMARY KEY,                        -- Уникальный ID поста (автоинкремент)
    title VARCHAR(255) NOT NULL,                   -- Заголовок (обязательный)
    text TEXT NOT NULL,                           -- Текст поста (обязательный)
    tags TEXT[] DEFAULT '{}',                     -- Массив тегов (по умолчанию пустой)
    likes_count INTEGER DEFAULT 0               -- Количество лайков (по умолчанию 0)
 );
CREATE TABLE IF NOT EXISTS comments (
    id BIGSERIAL PRIMARY KEY,                    -- Уникальный ID комментария
    post_id BIGINT NOT NULL,                  -- Внешний ключ на пост
    text TEXT NOT NULL,                         -- Текст комментария (обязательный)
 -- Внешний ключ с каскадным удалением
    CONSTRAINT fk_comments_post
        FOREIGN KEY (post_id)
        REFERENCES posts (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_posts_title ON posts (title);
CREATE INDEX IF NOT EXISTS idx_posts_tags ON posts (tags);
CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments (post_id);
