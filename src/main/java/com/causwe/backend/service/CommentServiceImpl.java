package com.causwe.backend.service;

import com.causwe.backend.exceptions.CommentNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Comment;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final IssueService issueService;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserService userService, IssueService issueService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.issueService = issueService;
    }

    @Override
    public List<Comment> getAllComments(Long issueId) {
        Issue issue = issueService.getIssueById(issueId);
        return commentRepository.findByIssueOrderByCreatedAtAsc(issue);
    }

    @Override
    public Comment addComment(Long issueId, Comment commentData, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Issue issue = issueService.getIssueById(issueId);

        if (issue != null) {
            Comment comment = new Comment(issue, currentUser, commentData.getContent());
            return commentRepository.save(comment);
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteComment(Long id, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        if(currentUser.canDeleteComment(comment)){
            commentRepository.deleteById(id);
            return true;
        }else{
            throw new UnauthorizedException("You are not authorized to delete this comment.");
        }
    }

    @Override
    public Comment updateComment(Long id, Comment commentData, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("Only the author of the comment can update the comment.");
        }

        comment.setContent(commentData.getContent());
        return commentRepository.save(comment);
    }
}
