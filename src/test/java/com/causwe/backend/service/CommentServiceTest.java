package com.causwe.backend.service;

import com.causwe.backend.exceptions.CommentNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Admin;
import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Developer;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.CommentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private IssueService issueService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User admin;
    private User dev;
    private Issue issue;
    private Comment comment1;
    private Comment comment2;

    @BeforeEach
    public void setUp() {

        admin = new Admin();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setId(1L);

        dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(2L);

        issue = new Issue();
        issue.setId(1L);
        issue.setTitle("Test Issue");

        comment1 = new Comment(issue, admin, "Test Comment1");
        comment1.setId(1L);
        comment1.setCreatedAt(LocalDateTime.of(2024, 5, 28, 10, 0, 0));

        comment2 = new Comment(issue, dev, "Test Comment2");
        comment2.setId(2L);
        comment2.setCreatedAt(LocalDateTime.of(2024, 5, 28, 13, 0, 0));
    }

    @Test
    public void testGetAllComments() {
        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);
        commentList.add(comment2);

        when(issueService.getIssueById(1L)).thenReturn(issue);
        when(commentRepository.findByIssueOrderByCreatedAtAsc(issue)).thenReturn(commentList);

        List<Comment> comments = commentService.getAllComments(1L);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals("Test Comment1", comments.get(0).getContent());
    }

    @Test
    public void testAddComment_Success() {
        when(userService.getUserById(2L)).thenReturn(dev);
        when(issueService.getIssueById(1L)).thenReturn(issue);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment1);

        Comment addedComment = commentService.addComment(1L, comment1, 2L);
        assertNotNull(addedComment);
        assertEquals("Test Comment1", addedComment.getContent());
    }

    @Test
    public void testAddComment_Unauthorized() {
        when(userService.getUserById(3L)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            commentService.addComment(1L, comment1, 3L);
        });
    }

    @Test
    public void testDeleteComment_Success_AsAdmin() {
        when(userService.getUserById(1L)).thenReturn(admin);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        boolean result = commentService.deleteComment(1L, 1L);
        assertTrue(result);
        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteComment_Success_AsAuthor() {
        when(userService.getUserById(2L)).thenReturn(dev);
        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment2));

        boolean result = commentService.deleteComment(2L, 2L);
        assertTrue(result);
        verify(commentRepository, times(1)).deleteById(2L);
    }

    @Test
    public void testDeleteComment_Unauthorized_NotPermitted() {
        when(userService.getUserById(2L)).thenReturn(dev);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        assertThrows(UnauthorizedException.class, () -> {
            commentService.deleteComment(1L, 2L);
        });
    }

    @Test
    public void testDeleteComment_Unauthorized_NotLoggedIn() {
        when(userService.getUserById(null)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            commentService.deleteComment(1L, null);
        });
    }

    @Test
    public void testDeleteComment_NotFound() {
        when(userService.getUserById(2L)).thenReturn(dev);
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> {
            commentService.deleteComment(1L, 2L);
        });
    }

    @Test
    public void testUpdateComment_Success() {
        Comment updatedData = new Comment();
        updatedData.setContent("Updated Comment");

        when(userService.getUserById(2L)).thenReturn(dev);
        when(commentRepository.findById(2L)).thenReturn(Optional.of(comment2));
        when(commentRepository.save(comment2)).thenReturn(comment2);

        Comment updatedComment = commentService.updateComment(2L, updatedData, 2L);
        assertNotNull(updatedComment);
        assertEquals("Updated Comment", updatedComment.getContent());
    }

    @Test
    public void testUpdateComment_Unauthorized_NotPermitted() {
        Comment updatedData = new Comment();
        updatedData.setContent("Updated Comment");

        when(userService.getUserById(2L)).thenReturn(dev);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment1));

        assertThrows(UnauthorizedException.class, () -> {
            commentService.updateComment(1L, updatedData, 2L);
        });
    }

    @Test
    public void testUpdateComment_Unauthorized_NotLoggedIn() {
        Comment updatedData = new Comment();
        updatedData.setContent("Updated Comment");

        when(userService.getUserById(null)).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> {
            commentService.updateComment(1L, updatedData, null);
        });
    }

    @Test
    public void testUpdateComment_NotFound() {
        Comment updatedData = new Comment();
        updatedData.setContent("Updated Comment");

        when(userService.getUserById(2L)).thenReturn(dev);
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class, () -> {
            commentService.updateComment(1L, updatedData, 2L);
        });
    }
}
