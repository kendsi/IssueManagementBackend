package com.causwe.backend.controller;

import com.causwe.backend.dto.CommentDTO;
import com.causwe.backend.exceptions.CommentNotFoundException;
import com.causwe.backend.exceptions.GlobalExceptionHandler;
import com.causwe.backend.exceptions.IssueNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Developer;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.CommentService;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private CommentService commentService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private CommentController commentController;

    private User tester;
    private Issue issue;
    private Comment comment;
    private CommentDTO commentDTO;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        
        objectMapper = new ObjectMapper();

        issue = new Issue("Test Issue1", "Issue Description", tester);
        issue.setId(1L);

        comment = new Comment(issue, tester, "Test Comment");
        comment.setId(1L);

        commentDTO = new CommentDTO();
        commentDTO.setId(1L);
        commentDTO.setContent("Test Comment");
    }

    @Test
    public void testGetAllComment() throws Exception {
        List<Comment> comments = new ArrayList<>();
        comments.add(comment);
        
        when(modelMapper.map(comment, CommentDTO.class)).thenReturn(commentDTO);

        when(commentService.getAllComments(1L)).thenReturn(comments);

        mockMvc.perform(get("/api/projects/1/issues/1/comments")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(comment.getId().intValue()))
                .andExpect(jsonPath("$[0].content").value(comment.getContent()));
    }

    @Test
    public void testAddComment_Success() throws Exception {

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(modelMapper.map(comment, CommentDTO.class)).thenReturn(commentDTO);
        when(commentService.addComment(1L, comment, 3L)).thenReturn(comment);

        mockMvc.perform(post("/api/projects/1/issues/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(comment.getId().intValue()))
                .andExpect(jsonPath("$.content").value(comment.getContent()));
    }

    @Test
    public void testAddComment_BadRequest() throws Exception {
        commentDTO.setContent("");

        mockMvc.perform(post("/api/projects/1/issues/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddComment_NotFound() throws Exception {

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(commentService.addComment(2L, comment, 3L)).thenThrow(new IssueNotFoundException(2L));

        mockMvc.perform(post("/api/projects/1/issues/2/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testAddComment_Unauthorized() throws Exception {
        
        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(commentService.addComment(1L, comment, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(post("/api/projects/1/issues/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "")))
                .andExpect(status().isUnauthorized());
    }


    @Test
    public void testUpdateComment_Success() throws Exception {
        commentDTO.setContent("Updated Comment");
        comment.setContent("Updated Comment");

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(modelMapper.map(any(Comment.class), eq(CommentDTO.class))).thenReturn(commentDTO);

        when(commentService.updateComment(1L, comment, 3L)).thenReturn(comment);

        mockMvc.perform(put("/api/projects/1/issues/1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId().intValue()))
                .andExpect(jsonPath("$.content").value(comment.getContent()));
    }

    @Test
    public void testUpdateComment_BadRequest() throws Exception {
        commentDTO.setContent("");

        mockMvc.perform(put("/api/projects/1/issues/1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateComment_NotFound() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(commentService.updateComment(1L, comment, 3L)).thenThrow(new CommentNotFoundException(1L));

        mockMvc.perform(put("/api/projects/1/issues/1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateComment_Unauthorized_NotPermitted() throws Exception {
        User dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(2L);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(2L);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(commentService.updateComment(1L, comment, 2L)).thenThrow(new UnauthorizedException("Only the author of the comment can update the comment."));

        mockMvc.perform(put("/api/projects/1/issues/1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateComment_Unauthorized_NotLoggedIn() throws Exception {

        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(modelMapper.map(any(CommentDTO.class), eq(Comment.class))).thenReturn(comment);
        when(commentService.updateComment(1L, comment, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(put("/api/projects/1/issues/1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentDTO))
                .cookie(new Cookie("jwt", "")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteComment_Success() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(commentService.deleteComment(1L, 3L)).thenReturn(true);

        mockMvc.perform(delete("/api/projects/1/issues/1/comments/1")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteComment_NotFound() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(3L);
        when(commentService.deleteComment(1L, 3L)).thenThrow(new CommentNotFoundException(1L));

        mockMvc.perform(delete("/api/projects/1/issues/1/comments/1")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteComment_Unauthorized_NotPermitted() throws Exception {
        User dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(2L);

        when(jwtTokenProvider.getUserIdFromToken("token")).thenReturn(2L);
        when(commentService.deleteComment(1L, 2L)).thenThrow(new UnauthorizedException("You are not authorized to delete this comment."));

        mockMvc.perform(delete("/api/projects/1/issues/1/comments/1")
                .cookie(new Cookie("jwt", "token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDeleteComment_Unauthorized_NotLoggedIn() throws Exception {
        User dev = new Developer();
        dev.setUsername("dev");
        dev.setPassword("dev");
        dev.setId(2L);

        when(jwtTokenProvider.getUserIdFromToken("")).thenReturn(null);
        when(commentService.deleteComment(1L, null)).thenThrow(new UnauthorizedException("User not logged in"));

        mockMvc.perform(delete("/api/projects/1/issues/1/comments/1")
                .cookie(new Cookie("jwt", "")))
                .andExpect(status().isUnauthorized());
    }
}