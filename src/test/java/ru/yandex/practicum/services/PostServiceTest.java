package ru.yandex.practicum.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.practicum.dto.*;
import ru.yandex.practicum.model.Comment;
import ru.yandex.practicum.model.Post;
import ru.yandex.practicum.repository.PostRepository;
import ru.yandex.practicum.service.PostService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = PostService.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PostServiceTest {

    @MockitoBean
    private PostRepository postRepository;

    @Autowired
    private PostService postService;

    private static final Long POST_ID = 1L;
    private static final Long COMMENT_ID = 101L;

    private Post post;
    private PostRequest postRequest;
    private Comment comment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        post = new Post(POST_ID, "Test Title", "Test Content", Arrays.asList("tag1", "tag2"), 5);
        postRequest = new PostRequest("New Title", "New Content", List.of("newTag"));
        comment = new Comment(COMMENT_ID, POST_ID, "Comment Text");
        commentRequest = new CommentRequest("Updated Comment Text");
    }

    @Test
    void getPosts_withSearchAndPagination_returnsTruncatedTextIfLong() {
        
        String search = "test";
        int pageNumber = 0;
        int pageSize = 10;

        String longText = "A".repeat(150); // текст длиннее 128 символов
        PostResponse longPost = new PostResponse(2L, "Long Post", longText, Collections.emptyList(), 0, 0);
        PostsPageResponse page = new PostsPageResponse(List.of(longPost), false, false, 1);

        when(postRepository.findPosts(search, pageNumber, pageSize)).thenReturn(page);

       
        PostsPageResponse result = postService.getPosts(search, pageNumber, pageSize);

        assertNotNull(result);
        assertEquals(1, result.getPosts().size());

        PostResponse truncatedPost = result.getPosts().getFirst();
        assertTrue(truncatedPost.getText().length() <= 128+("...").length());
        assertTrue(truncatedPost.getText().endsWith("..."));

        verify(postRepository, times(1)).findPosts(search, pageNumber, pageSize);
    }

    @Test
    void getPost_existingPost_returnsPostResponse() {
        
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postRepository.getCommentsCount(POST_ID)).thenReturn(3);

       
        PostResponse result = postService.getPost(POST_ID);

       
        assertNotNull(result);
        assertEquals(POST_ID, result.getId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Content", result.getText());
        assertEquals(Arrays.asList("tag1", "tag2"), result.getTags());
        assertEquals(5, result.getLikesCount());
        assertEquals(3, result.getCommentsCount());

        verify(postRepository, times(1)).findById(POST_ID);
        verify(postRepository, times(1)).getCommentsCount(POST_ID);
    }

    @Test
    void getPost_nonExistingPost_returnsNull() {
        
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

       
        PostResponse result = postService.getPost(POST_ID);

       
        assertNull(result);

        verify(postRepository, times(1)).findById(POST_ID);
        verify(postRepository, never()).getCommentsCount(anyLong());
    }

    @Test
    void createPost_validRequest_returnsPostResponse() {
        
        Post savedPost = new Post(2L, "New Title", "New Content", List.of("newTag"), 0);

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

       
        PostResponse result = postService.createPost(postRequest);

       
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getText());
        assertEquals(List.of("newTag"), result.getTags());
        assertEquals(0, result.getLikesCount());
        assertEquals(0, result.getCommentsCount());

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_existingPost_updatesAndReturnsPostResponse() {
        
        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(post));
        when(postRepository.getCommentsCount(POST_ID)).thenReturn(2);

       
        PostResponse result = postService.updatePost(POST_ID, postRequest);

       
        assertNotNull(result);
        assertEquals(POST_ID, result.getId());
        assertEquals("New Title", result.getTitle());
        assertEquals("New Content", result.getText());
        assertEquals(List.of("newTag"), result.getTags());
        assertEquals(5, result.getLikesCount()); // лайки не меняются
        assertEquals(2, result.getCommentsCount());

        verify(postRepository, times(1)).findById(POST_ID);
        verify(postRepository, times(1)).update(any(Post.class));
        verify(postRepository, times(1)).getCommentsCount(POST_ID);
    }

    @Test
    void updatePost_nonExistingPost_returnsNull() {
        
        when(postRepository.findById(POST_ID)).thenReturn(Optional.empty());

       
        PostResponse result = postService.updatePost(POST_ID, postRequest);

       
        assertNull(result);

        verify(postRepository, times(1)).findById(POST_ID);
        verify(postRepository, never()).update(any(Post.class));
    }

    @Test
    void deletePost_existingPost_returnsTrue() {
        
        when(postRepository.deleteById(POST_ID)).thenReturn(true);

       
        boolean result = postService.deletePost(POST_ID);

       
        assertTrue(result);

        verify(postRepository, times(1)).deleteById(POST_ID);
    }

    @Test
    void deletePost_nonExistingPost_returnsFalse() {
        
        when(postRepository.deleteById(POST_ID)).thenReturn(false);

       
        boolean result = postService.deletePost(POST_ID);

       
        assertFalse(result);

        verify(postRepository, times(1)).deleteById(POST_ID);
    }

    @Test
    void incrementLikes_existingPost_incrementsAndReturnsNewCount() {
        
        when(postRepository.incrementLikes(POST_ID)).thenReturn(6);

       
        int result = postService.incrementLikes(POST_ID);

       
        assertEquals(6, result);

        verify(postRepository, times(1)).incrementLikes(POST_ID);
    }


    @Test
    void getComments_existingPost_returnsCommentsList() {
        
        List<Comment> comments = Arrays.asList(
                new Comment(101L, POST_ID, "First comment"),
                new Comment(102L, POST_ID, "Second comment")
        );
        when(postRepository.findCommentsByPostId(POST_ID)).thenReturn(comments);

       
        List<CommentResponse> result = postService.getComments(POST_ID);

       
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("First comment", result.getFirst().getText());
        assertEquals(POST_ID, result.getFirst().getPostId());

        verify(postRepository, times(1)).findCommentsByPostId(POST_ID);
    }

    @Test
    void getComments_noComments_returnsEmptyList() {
        
        when(postRepository.findCommentsByPostId(POST_ID)).thenReturn(Collections.emptyList());

       
        List<CommentResponse> result = postService.getComments(POST_ID);

       
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(postRepository, times(1)).findCommentsByPostId(POST_ID);
    }

    @Test
    void getComment_existingComment_returnsCommentResponse() {
        
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.of(comment));

       
        CommentResponse result = postService.getComment(POST_ID, COMMENT_ID);

       
        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getId());
        assertEquals("Comment Text", result.getText());
        assertEquals(POST_ID, result.getPostId());

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
    }

    @Test
    void getComment_commentDoesNotBelongToPost_returnsNull() {
        
        Comment otherPostComment = new Comment(COMMENT_ID, 999L, "Other post comment");
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.of(otherPostComment));

       
        CommentResponse result = postService.getComment(POST_ID, COMMENT_ID);

       
        assertNull(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
    }

    @Test
    void getComment_nonExistingComment_returnsNull() {
        
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.empty());

       
        CommentResponse result = postService.getComment(POST_ID, COMMENT_ID);

       
        assertNull(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
    }

    @Test
    void addComment_existingPost_addsAndReturnsCommentResponse() {
        
        when(postRepository.existsById(POST_ID)).thenReturn(true);
        when(postRepository.insertComment(any(Comment.class))).thenReturn(COMMENT_ID);

       
        CommentResponse result = postService.addComment(POST_ID, commentRequest);

       
        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getId());
        assertEquals("Updated Comment Text", result.getText());
        assertEquals(POST_ID, result.getPostId());

        verify(postRepository, times(1)).existsById(POST_ID);
        verify(postRepository, times(1)).insertComment(any(Comment.class));
    }

    @Test
    void addComment_nonExistingPost_returnsNull() {
        
        when(postRepository.existsById(POST_ID)).thenReturn(false);

       
        CommentResponse result = postService.addComment(POST_ID, commentRequest);

       
        assertNull(result);

        verify(postRepository, times(1)).existsById(POST_ID);
        verify(postRepository, never()).insertComment(any(Comment.class));
    }

    @Test
    void updateComment_existingComment_updatesAndReturnsCommentResponse() {
        
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.of(comment));

       
        CommentResponse result = postService.updateComment(POST_ID, COMMENT_ID, commentRequest);

       
        assertNotNull(result);
        assertEquals(COMMENT_ID, result.getId());
        assertEquals("Updated Comment Text", result.getText());
        assertEquals(POST_ID, result.getPostId());

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
        verify(postRepository, times(1)).updateComment(any(Comment.class));
    }

    @Test
    void updateComment_commentDoesNotBelongToPost_returnsNull() {
        
        Comment otherPostComment = new Comment(COMMENT_ID, 999L, "Other post comment");
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.of(otherPostComment));

       
        CommentResponse result = postService.updateComment(POST_ID, COMMENT_ID, commentRequest);

       
        assertNull(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
        verify(postRepository, never()).updateComment(any(Comment.class));
    }

    @Test
    void updateComment_nonExistingComment_returnsNull() {
        
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.empty());

       
        CommentResponse result = postService.updateComment(POST_ID, COMMENT_ID, commentRequest);

       
        assertNull(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
        verify(postRepository, never()).updateComment(any(Comment.class));
    }

    @Test
    void deleteComment_existingCommentBelongingToPost_deletesAndReturnsTrue() {
        
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.of(comment));
        when(postRepository.deleteComment(COMMENT_ID)).thenReturn(true);

       
        boolean result = postService.deleteComment(POST_ID, COMMENT_ID);

       
        assertTrue(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
        verify(postRepository, times(1)).deleteComment(COMMENT_ID);
    }

    @Test
    void deleteComment_commentDoesNotBelongToPost_returnsFalse() {
        
        Comment otherPostComment = new Comment(COMMENT_ID, 999L, "Other post comment");
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.of(otherPostComment));


       
        boolean result = postService.deleteComment(POST_ID, COMMENT_ID);

       
        assertFalse(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
        verify(postRepository, never()).deleteComment(anyLong());
    }

    @Test
    void deleteComment_nonExistingComment_returnsFalse() {
        
        when(postRepository.findCommentById(COMMENT_ID)).thenReturn(Optional.empty());

       
        boolean result = postService.deleteComment(POST_ID, COMMENT_ID);

       
        assertFalse(result);

        verify(postRepository, times(1)).findCommentById(COMMENT_ID);
        verify(postRepository, never()).deleteComment(anyLong());
    }

    @Test
    void getPosts_emptySearchAndPagination_returnsAllPosts() {
        
        String search = "";
        int pageNumber = 0;
        int pageSize = 5;

        List<PostResponse> posts = Arrays.asList(
                new PostResponse(1L, "Post 1", "Short text", Collections.emptyList(), 2, 0),
                new PostResponse(2L, "Post 2", "Another short text", Collections.emptyList(), 7, 0)
        );
        PostsPageResponse page = new PostsPageResponse(posts, false, false, 1);

        when(postRepository.findPosts(search, pageNumber, pageSize)).thenReturn(page);

       
        PostsPageResponse result = postService.getPosts(search, pageNumber, pageSize);

       
        assertNotNull(result);
        assertEquals(2, result.getPosts().size());
        assertEquals("Post 1", result.getPosts().getFirst().getTitle());

        verify(postRepository, times(1)).findPosts(search, pageNumber, pageSize);
    }

    @Test
    void getPosts_longText_truncatesTextTo128Characters() {
        
        String longText = "A".repeat(200); // текст из 200 символов
        PostResponse longPost = new PostResponse(3L, "Long Post", longText, Collections.emptyList(), 10, 0);
        PostsPageResponse page = new PostsPageResponse(List.of(longPost), false, false, 1);

        when(postRepository.findPosts("", 0, 10)).thenReturn(page);

       
        PostsPageResponse result = postService.getPosts("", 0, 10);

       
        assertNotNull(result);
        assertEquals(1, result.getPosts().size());

        PostResponse truncatedPost = result.getPosts().getFirst();
        assertEquals(128 + ("...").length(), truncatedPost.getText().length());
        assertTrue(truncatedPost.getText().endsWith("..."));

        verify(postRepository, times(1)).findPosts("", 0, 10);
    }

    @Test
    void createPost_withNullValues_createsPostWithDefaultValues() {
        
        PostRequest nullRequest = new PostRequest(null, null, null);
        Post savedPost = new Post(4L, null, null, null, 0);

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

       
        PostResponse result = postService.createPost(nullRequest);

       
        assertNotNull(result);
        assertEquals(4L, result.getId());
        assertNull(result.getTitle());
        assertNull(result.getText());
        assertNull(result.getTags());
        assertEquals(0, result.getLikesCount());
        assertEquals(0, result.getCommentsCount());

        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_withEmptyTags_updatesTagsToEmptyList() {
        
        Post existingPost = new Post(POST_ID, "Existing", "Content", List.of("oldTag"), 5);
        PostRequest updateRequest = new PostRequest("Updated", "Updated Content", Collections.emptyList());

        when(postRepository.findById(POST_ID)).thenReturn(Optional.of(existingPost));
        when(postRepository.getCommentsCount(POST_ID)).thenReturn(3);

       
        PostResponse result = postService.updatePost(POST_ID, updateRequest);

       
        assertNotNull(result);
        assertEquals(List.of(), result.getTags());

        verify(postRepository, times(1)).update(any(Post.class));
    }

    @Test
    void incrementLikes_nonExistingPost_returnsZero() {
        
        when(postRepository.incrementLikes(POST_ID)).thenReturn(0); // репозиторий возвращает 0 для несуществующего поста

       
        int result = postService.incrementLikes(POST_ID);

       
        assertEquals(0, result);

        verify(postRepository, times(1)).incrementLikes(POST_ID);
    }
}