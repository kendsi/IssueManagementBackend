package com.causwe.backend.controller;

import com.causwe.backend.dto.CommentDTO;
import com.causwe.backend.exceptions.CommentNotFoundException;
import com.causwe.backend.exceptions.IssueNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Comment;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.CommentService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    public ResponseEntity<List<CommentDTO>> getAllComment(@PathVariable Long issueId) {
        List<Comment> comments = commentService.getAllComments(issueId);

        List<CommentDTO> commentDTOs = comments
                .stream()
                .map(comment -> modelMapper.map(comment, CommentDTO.class))
                .collect(Collectors.toList());

        return new ResponseEntity<>(commentDTOs, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long issueId, @RequestBody CommentDTO commentData, @CookieValue(name = "jwt", required = false) String token) {
        if (Objects.equals(commentData.getContent(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            Comment comment = commentService.addComment(issueId, modelMapper.map(commentData, Comment.class), memberId);
            CommentDTO commentDTO = modelMapper.map(comment, CommentDTO.class);
            return new ResponseEntity<>(commentDTO, HttpStatus.CREATED);
        } catch (IssueNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id, @RequestBody CommentDTO updatedComment, @CookieValue(name = "jwt", required = false) String token) {
        if (Objects.equals(updatedComment.getContent(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            Comment updated = commentService.updateComment(id, modelMapper.map(updatedComment, Comment.class), memberId);
            CommentDTO updatedDTO = modelMapper.map(updated, CommentDTO.class);
            return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @CookieValue(name = "jwt", required = false) String token) {
        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            commentService.deleteComment(id, memberId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (CommentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
