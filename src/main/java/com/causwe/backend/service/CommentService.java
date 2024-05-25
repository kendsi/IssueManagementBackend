package com.causwe.backend.service;

import com.causwe.backend.model.Comment;
import java.util.List;

public interface CommentService {
    List<Comment> getAllComments(Long issueId);
    Comment addComment(Long issueId, Comment commentData, Long memberId);
    boolean deleteComment(Long id, Long memberId);
    Comment updateComment(Long id, Comment commentData, Long memberId);
}