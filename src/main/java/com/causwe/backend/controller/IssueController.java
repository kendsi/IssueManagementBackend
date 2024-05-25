package com.causwe.backend.controller;

import com.causwe.backend.dto.IssueDTO;
import com.causwe.backend.dto.UserDTO;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.service.IssueService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<List<IssueDTO>> getAllIssues(@PathVariable Long projectId, @CookieValue(value = "memberId", required = false) Long memberId) {
        List<Issue> issues = issueService.getAllIssues(projectId, memberId);
        
        List<IssueDTO> issueDTOs = issues
        .stream()
        .map(issue -> modelMapper.map(issue, IssueDTO.class))
        .collect(Collectors.toList());

        return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDTO> getIssueById(@PathVariable Long id) {
        Issue issue = issueService.getIssueById(id);

        if (issue != null) {
            IssueDTO issueDTO = modelMapper.map(issue, IssueDTO.class);
            return new ResponseEntity<>(issueDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("")
    public ResponseEntity<IssueDTO> createIssue(@PathVariable Long projectId, @RequestBody IssueDTO issueData, @CookieValue(value = "memberId", required = false) Long memberId) {
        if (Objects.equals(issueData.getTitle(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Issue newIssue = issueService.createIssue(projectId, modelMapper.map(issueData, Issue.class), memberId);
        IssueDTO newIssueDTO = modelMapper.map(newIssue, IssueDTO.class);

        return new ResponseEntity<>(newIssueDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueDTO> updateIssue(@PathVariable Long id, @RequestBody IssueDTO updatedIssue, @CookieValue(value = "memberId", required = false) Long memberId) {
        if (Objects.equals(updatedIssue.getTitle(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Issue updated = issueService.updateIssue(id, modelMapper.map(updatedIssue, Issue.class), memberId);
        IssueDTO updatedDTO = modelMapper.map(updated, IssueDTO.class);

        if (updated != null) {
            return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<IssueDTO>> searchIssues(@PathVariable Long projectId, @RequestBody IssueDTO issueData, @CookieValue(value = "memberId", required = false) Long memberId) {
        List<Issue> issues = issueService.searchIssues(projectId, modelMapper.map(issueData, Issue.class), memberId);

        List<IssueDTO> issueDTOs = issues
        .stream()
        .map(issue -> modelMapper.map(issue, IssueDTO.class))
        .collect(Collectors.toList());

        return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
    }

    @GetMapping("/searchbynl")
    public ResponseEntity<List<IssueDTO>> searchIssuesbyNL(@PathVariable Long projectId,
                                                           @RequestParam(value = "userMessage") String userMessage,
                                                           @CookieValue(value = "memberId", required = false) Long memberId) {
        try {
            List<Issue> issues = issueService.searchIssuesByNL(projectId, userMessage, memberId);
            List<IssueDTO> issueDTOs = issues.stream()
                    .map(issue -> modelMapper.map(issue, IssueDTO.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
        } catch (IOException e) {
            // 예외 처리 로직 추가
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/recommendedAssignees")
    public ResponseEntity<List<UserDTO>> getRecommendedAssignees(@PathVariable Long projectId, @PathVariable Long id) {
        List<User> recommendedAssignees = issueService.getRecommendedAssignees(projectId, id);
        
        if (recommendedAssignees != null) {
            List<UserDTO> userDTOs = recommendedAssignees
            .stream()
            .map(user -> modelMapper.map(user, UserDTO.class))
            .collect(Collectors.toList());

            return new ResponseEntity<>(userDTOs, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
