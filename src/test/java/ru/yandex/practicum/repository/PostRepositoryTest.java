package ru.yandex.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.practicum.dto.PostsPageResponse;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class PostRepositoryTest {

    private static final String TEST_TITLE = "Test Post Title";
    private static final String TEST_TEXT = "Test post text";
    private static final List<String> TEST_TAGS = List.of("#tag1", "#tag2");

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Container
    static PostgreSQLContainer<?> postgres;

    static {
        String image = System.getProperty("test.container.image", "postgres:18");
        postgres = new PostgreSQLContainer<>(image);
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        // Очищаем таблицы перед каждым тестом
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM posts");

    }

    @Test
    void findPosts_withNoSearch_returnsAllPostsWithPagination() {
        Post post1 = new Post(null, "First Post", "text 1", List.of("#tag1"), 5);
        Post post2 = new Post(null, "Second Post", "text 2", List.of("#tag2"), 10);
        postRepository.save(post1);
        postRepository.save(post2);

        PostsPageResponse result = postRepository.findPosts("", 1, 10);

        
        assertThat(result).isNotNull();
        assertThat(result.getPosts()).hasSize(2);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.isHasPrev()).isFalse();
    }

    @Test
    void findPosts_withSearchTerm_returnsFilteredResults() {
        Post post1 = new Post(null, "Post1", "text about Java", List.of("#tag1", "#tag2"), 5);
        Post post2 = new Post(null, "Post2", "Spring tutorial", List.of("#tag1", "#tag3"), 10);
        postRepository.save(post1);
        postRepository.save(post2);

        PostsPageResponse result = postRepository.findPosts("#tag1", 1, 10);

        assertThat(result.getPosts()).hasSize(2); // оба поста содержат тег "#tag1"
    }

    @Test
    void findById_existingPost_returnsPost() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));

        Optional<Post> found = postRepository.findById(savedPost.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo(TEST_TITLE);
        assertThat(found.get().getTags()).containsExactlyInAnyOrder("#tag1", "#tag2");
    }

    @Test
    void findById_nonExistingPost_returnsEmptyOptional() {

        Optional<Post> found = postRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void save_newPost_persistsAndReturnsId() {
        Post post = new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 5);
        Post saved = postRepository.save(post);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo(TEST_TITLE);
        assertThat(saved.getLikesCount()).isEqualTo(5);
    }

    @Test
    void update_existingPost_updatesFields() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        savedPost.setTitle("Updated Title");
        savedPost.setText("Updated text");
        savedPost.setTags(List.of("updated-tag"));

        postRepository.update(savedPost);

        Optional<Post> updated = postRepository.findById(savedPost.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Updated Title");
        assertThat(updated.get().getText()).isEqualTo("Updated text");
        assertThat(updated.get().getTags()).containsExactly("updated-tag");
    }

    @Test
    void deleteById_existingPost_deletesAndReturnsTrue() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        boolean result = postRepository.deleteById(savedPost.getId());

        assertThat(result).isTrue();
        Optional<Post> deleted = postRepository.findById(savedPost.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void incrementLikes_existingPost_incrementsAndReturnsNewCount() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 5));
        int newLikes = postRepository.incrementLikes(savedPost.getId());

        assertThat(newLikes).isEqualTo(6);
        Optional<Post> updatedPost = postRepository.findById(savedPost.getId());
        assertThat(updatedPost).isPresent();
        assertThat(updatedPost.get().getLikesCount()).isEqualTo(6);
    }

    @Test
    void findCommentsByPostId_existingPostWithComments_returnsComments() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        Comment comment1 = new Comment(null, savedPost.getId(), "First comment");
        Comment comment2 = new Comment(null, savedPost.getId(), "Second comment");

        postRepository.insertComment(comment1);
        postRepository.insertComment(comment2);

        List<Comment> comments = postRepository.findCommentsByPostId(savedPost.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getText()).isEqualTo("Second comment");
        assertThat(comments.get(1).getText()).isEqualTo("First comment");
    }

    @Test
    void existsById_existingPost_returnsTrue() {

        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        boolean exists = postRepository.existsById(savedPost.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void getCommentsCount_postWithComments_returnsCorrectCount() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        postRepository.insertComment(new Comment(null, savedPost.getId(), "Comment 1"));
        postRepository.insertComment(new Comment(null, savedPost.getId(), "Comment 2"));

        int count = postRepository.getCommentsCount(savedPost.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    void insertComment_newComment_insertsAndReturnsId() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        Long commentId = postRepository.insertComment(
                new Comment(null, savedPost.getId(), "Test comment text")
        );

        assertThat(commentId).isNotNull();
        assertThat(commentId).isGreaterThan(0L);

        // Проверяем, что комментарий действительно сохранён
        Optional<Comment> savedComment = postRepository.findCommentById(commentId);
        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().getText()).isEqualTo("Test comment text");
    }

    @Test
    void updateComment_existingComment_updatesText() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        Long commentId = postRepository.insertComment(
                new Comment(null, savedPost.getId(), "Original comment")
        );

        Comment updatedComment = new Comment(commentId, savedPost.getId(), "Updated comment text");

        postRepository.updateComment(updatedComment);

        Optional<Comment> updated = postRepository.findCommentById(commentId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getText()).isEqualTo("Updated comment text");
    }

    @Test
    void deleteComment_existingComment_deletesAndReturnsTrue() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        Long commentId = postRepository.insertComment(
                new Comment(null, savedPost.getId(), "Comment to delete")
        );

        boolean result = postRepository.deleteComment(commentId);

        assertThat(result).isTrue();

        Optional<Comment> deleted = postRepository.findCommentById(commentId);
        assertThat(deleted).isEmpty();
    }

    @Test
    void findCommentById_existingComment_returnsComment() {
        Post savedPost = postRepository.save(new Post(null, TEST_TITLE, TEST_TEXT, TEST_TAGS, 0));
        Long commentId = postRepository.insertComment(
                new Comment(null, savedPost.getId(), "Specific comment")
        );

        Optional<Comment> found = postRepository.findCommentById(commentId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(commentId);
        assertThat(found.get().getText()).isEqualTo("Specific comment");
    }

    @Test
    void findCommentById_nonExistingComment_returnsEmptyOptional() {
        Optional<Comment> found = postRepository.findCommentById(999L);
        assertThat(found).isEmpty();
    }
}