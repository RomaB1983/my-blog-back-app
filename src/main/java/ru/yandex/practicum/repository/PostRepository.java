package ru.yandex.practicum.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.dto.PostResponse;
import ru.yandex.practicum.dto.PostsPageResponse;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.PagenablePost;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.mapper.CommentRowMapper;
import ru.yandex.practicum.repository.mapper.PagenablePostRowMapper;
import ru.yandex.practicum.repository.mapper.PostRowMapper;
import ru.yandex.practicum.service.utils.StringUtils;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // 1. Получить ленту постов с пагинацией и поиском
    public PostsPageResponse findPosts(String search, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        // Помещаем в мапу с ключами tags и tittles элементы из строки поиска
        Map<String, String> result = StringUtils.splitByHash(search);

        String sql = """
                    SELECT id,
                           title,
                           text,
                           tags,
                           likes_count,
                           COUNT(1) OVER() total_count,
                           (SELECT COUNT(1) FROM comments c WHERE c.post_id=p.id) comments_count
                        FROM posts p
                    WHERE p.title ILIKE ?
                        AND ( STRING_TO_ARRAY(?,',') <@ p.tags
                            OR STRING_TO_ARRAY(?,',') = ARRAY[]::TEXT[] )
                    ORDER BY p.id DESC
                    LIMIT ? OFFSET ?
                """;

        List<PagenablePost> posts = jdbcTemplate.query(
                sql,
                new Object[]{
                        "%" + result.get("titles") + "%",
                        result.get("tags"),
                        result.get("tags"),
                        pageSize,
                        offset},
                new int[]{
                        java.sql.Types.VARCHAR,
                        java.sql.Types.VARCHAR,
                        java.sql.Types.VARCHAR,
                        Types.INTEGER,
                        Types.INTEGER
                },
                new PagenablePostRowMapper()
        );
        int lastPage = 0;
        if (! posts.isEmpty()) {
            lastPage = (int) Math.ceil((double) posts.getFirst().getTotalCount() / pageSize);
        }
        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        // Конвертируем Post → PostResponse с подсчётом комментариев
        List<PostResponse> responsePosts = posts.stream()
                .map(post -> new PostResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getText(),
                        post.getTags(),
                        post.getLikesCount(),
                        post.getCommentsCount()
                ))
                .collect(Collectors.toList());

        return new PostsPageResponse(responsePosts, hasPrev, hasNext, lastPage);
    }

    // 2. Получить пост по ID
    public Optional<Post> findById(Long id) {
        try {
            Post post = jdbcTemplate.queryForObject(
                    "SELECT * FROM posts WHERE id = ?",
                    new PostRowMapper(),
                    id
            );
            return Optional.ofNullable(post);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    // 3. Создать пост
    public Post save(Post post) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO posts (title, text, tags, likes_count) VALUES (?, ?, ?, ?)",
                            new String[]{"id"}
                    );
                    ps.setString(1, post.getTitle());
                    ps.setString(2, post.getText());
                    ps.setArray(3, connection.createArrayOf("text", post.getTags().toArray())); // PostgreSQL: массив строк
                    ps.setInt(4, post.getLikesCount());
                    return ps;
                },
                keyHolder
        );
        // Получаем сгенерированный ID
        post.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return post;
    }

    // 4. Обновить пост
    public void update(Post post) {
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "UPDATE posts SET title = ?, text = ?, tags = ? WHERE id = ?"
                    );
                    ps.setString(1, post.getTitle());
                    ps.setString(2, post.getText());
                    ps.setArray(3, connection.createArrayOf("text", post.getTags().toArray())); // PostgreSQL: массив строк
                    ps.setLong(4, post.getId());
                    return ps;
                }
        );
    }

    // 5. Удалить пост
    public boolean deleteById(Long id) {
        int rows = jdbcTemplate.update("DELETE FROM posts WHERE id = ?", id);
        return rows > 0;
    }

    // 6. Увеличить лайки
    public int incrementLikes(Long id) {
        jdbcTemplate.update("UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?", id);
        return jdbcTemplate.queryForObject(
                "SELECT likes_count FROM posts WHERE id = ?",
                Integer.class,
                id
        );
    }

    // 9. Получить комментарии поста
    public List<Comment> findCommentsByPostId(Long postId) {
        return jdbcTemplate.query(
                "SELECT * FROM comments WHERE post_id = ? ORDER BY id desc",
                new CommentRowMapper(),
                postId
        );
    }

    // 10. Проверить существование поста
    public boolean existsById(Long id) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM posts WHERE id = ?",
                Integer.class,
                id
        );
        return count != null && count > 0;
    }

    // 11. Получить количество комментариев для поста
    public int getCommentsCount(Long postId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM comments WHERE post_id = ?",
                Integer.class,
                postId
        );
    }

    // 12. Добавить комментарий (возвращает ID)
    public Long insertComment(Comment comment) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO comments (post_id, text) VALUES (?, ?)", new String[]{"id"}
                    );
                    ps.setLong(1, comment.getPostId());
                    ps.setString(2, comment.getText());
                    return ps;
                },
                keyHolder
        );
        // Получаем сгенерированный ID
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    // 13. Обновить комментарий
    public void updateComment(Comment comment) {
        jdbcTemplate.update(
                "UPDATE comments SET text = ? WHERE id = ? AND post_id = ?",
                comment.getText(), comment.getId(), comment.getPostId()
        );
    }

    // 14. Удалить комментарий
    public boolean deleteComment(Long commentId) {
        int rows = jdbcTemplate.update("DELETE FROM comments WHERE id = ?", commentId);
        return rows > 0;
    }

    // 15. Найти комментарий по ID
    public Optional<Comment> findCommentById(Long commentId) {
        try {
            Comment comment = jdbcTemplate.queryForObject(
                    "SELECT * FROM comments WHERE id = ?",
                    new CommentRowMapper(),
                    commentId
            );
            return Optional.ofNullable(comment);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
