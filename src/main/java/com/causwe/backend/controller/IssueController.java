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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects/{projectId}/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<List<IssueDTO>> getAllIssues(@PathVariable Long projectId) {
        List<Issue> issues = issueService.getAllIssues(projectId);
        
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
        Issue newIssue = issueService.createIssue(projectId, modelMapper.map(issueData, Issue.class), memberId);
        IssueDTO newIssueDTO = modelMapper.map(newIssue, IssueDTO.class);

        return new ResponseEntity<>(newIssueDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IssueDTO> updateIssue(@PathVariable Long id, @RequestBody IssueDTO updatedIssue, @CookieValue(value = "memberId", required = false) Long memberId) {
        Issue updated = issueService.updateIssue(id, modelMapper.map(updatedIssue, Issue.class), memberId);
        IssueDTO updatedDTO = modelMapper.map(updated, IssueDTO.class);

        if (updated != null) {
            return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<IssueDTO>> searchIssues(@PathVariable Long projectId,
                                                    @RequestParam(value = "assignee", required = false) Long assigneeId,
                                                    @RequestParam(value = "reporter", required = false) Long reporterId,
                                                    @RequestParam(value = "status", required = false) Issue.Status status) {
        List<Issue> issues = issueService.searchIssues(projectId, assigneeId, reporterId, status);

        List<IssueDTO> issueDTOs = issues
        .stream()
        .map(issue -> modelMapper.map(issue, IssueDTO.class))
        .collect(Collectors.toList());

        return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
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
