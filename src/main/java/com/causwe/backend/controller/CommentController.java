package com.causwe.backend.controller;

import com.causwe.backend.dto.CommentDTO;
import com.causwe.backend.model.Comment;
import com.causwe.backend.service.CommentService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/issues/{issueId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ModelMapper modelMapper;

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
    public ResponseEntity<CommentDTO> addComment(@PathVariable Long issueId, @RequestBody CommentDTO commentData, @CookieValue(value = "memberId", required = false) Long memberId) {

        Comment comment = commentService.addComment(issueId, modelMapper.map(commentData, Comment.class), memberId);
        
        if (comment != null) {
            CommentDTO commentDTO = modelMapper.map(comment, CommentDTO.class);
            return new ResponseEntity<>(commentDTO, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id, @CookieValue(value = "memberId", required = false) Long memberId) {
        boolean isDeleted = commentService.deleteComment(id, memberId);
        if (isDeleted) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
