package com.causwe.backend.controller;

import com.causwe.backend.dto.IssueDTO;
import com.causwe.backend.dto.UserResponseDTO;
import com.causwe.backend.exceptions.IssueNotFoundException;
import com.causwe.backend.exceptions.ProjectNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.User;
import com.causwe.backend.security.JwtTokenProvider;
import com.causwe.backend.service.IssueService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("")
    @Cacheable(value = "issues", key = "{#projectId, #memberId}")
    public ResponseEntity<List<IssueDTO>> getAllIssues(@PathVariable Long projectId, @CookieValue(name = "jwt", required = false) String token) {
        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            List<Issue> issues = issueService.getAllIssues(projectId, memberId);
            List<IssueDTO> issueDTOs = issues
                    .stream()
                    .map(issue -> modelMapper.map(issue, IssueDTO.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
        } catch (ProjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    @Cacheable(value = "issues", key = "#id")
    public ResponseEntity<IssueDTO> getIssueById(@PathVariable Long id) {
        try {
            Issue issue = issueService.getIssueById(id);
            IssueDTO issueDTO = modelMapper.map(issue, IssueDTO.class);
            return new ResponseEntity<>(issueDTO, HttpStatus.OK);
        } catch (IssueNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("")
    @CacheEvict(value = {"issuesPerMonth", "issues", "issuesBySearch", "issuesByNLSearch"}, allEntries = true)
    public ResponseEntity<IssueDTO> createIssue(@PathVariable Long projectId, @RequestBody IssueDTO issueData, @CookieValue(name = "jwt", required = false) String token) {
        if (Objects.equals(issueData.getTitle(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            Issue newIssue = issueService.createIssue(projectId, modelMapper.map(issueData, Issue.class), memberId);
            IssueDTO newIssueDTO = modelMapper.map(newIssue, IssueDTO.class);
            return new ResponseEntity<>(newIssueDTO, HttpStatus.CREATED);
        } catch (ProjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"issues", "issuesBySearch", "issuesByNLSearch"}, allEntries = true)
    public ResponseEntity<IssueDTO> updateIssue(@PathVariable Long id, @RequestBody IssueDTO updatedIssue, @CookieValue(name = "jwt", required = false) String token) {
        if (Objects.equals(updatedIssue.getTitle(), "")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            Issue updated = issueService.updateIssue(id, modelMapper.map(updatedIssue, Issue.class), memberId);
            IssueDTO updatedDTO = modelMapper.map(updated, IssueDTO.class);
            return new ResponseEntity<>(updatedDTO, HttpStatus.OK);
        } catch (IssueNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (UnauthorizedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/search")
    @Cacheable(value = "issuesBySearch", key = "{#projectId, #assigneeUsername, #reporterUsername, #status, #memberId}")
    public ResponseEntity<List<IssueDTO>> searchIssues(@PathVariable Long projectId,
                                                       @RequestParam(value = "assigneeUsername", required = false) String assigneeUsername,
                                                       @RequestParam(value = "reporterUsername", required = false) String reporterUsername,
                                                       @RequestParam(value = "status", required = false) Issue.Status status, @CookieValue(name = "jwt", required = false) String token) {
        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            List<Issue> issues = issueService.searchIssues(projectId, assigneeUsername, reporterUsername, status, memberId);
            List<IssueDTO> issueDTOs = issues
                    .stream()
                    .map(issue -> modelMapper.map(issue, IssueDTO.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
        } catch (ProjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/searchbynl")
    @Cacheable(value = "issuesByNLSearch", key = "{#projectId, #userMessage, #memberId}")
    public ResponseEntity<List<IssueDTO>> searchIssuesbyNL(@PathVariable Long projectId,
                                                           @RequestParam(value = "userMessage") String userMessage,
                                                           @CookieValue(name = "jwt", required = false) String token) {
        try {
            Long memberId = jwtTokenProvider.getUserIdFromToken(token);
            List<Issue> issues = issueService.searchIssuesByNL(projectId, userMessage, memberId);
            List<IssueDTO> issueDTOs = issues.stream()
                    .map(issue -> modelMapper.map(issue, IssueDTO.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(issueDTOs, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ProjectNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/recommendedAssignees")
    public ResponseEntity<List<UserResponseDTO>> getRecommendedAssignees(@PathVariable Long id) {
        try {
            List<User> recommendedAssignees = issueService.getRecommendedAssignees(id);
            List<UserResponseDTO> userDTOs = recommendedAssignees
                    .stream()
                    .map(user -> modelMapper.map(user, UserResponseDTO.class))
                    .collect(Collectors.toList());
            return new ResponseEntity<>(userDTOs, HttpStatus.OK);
        } catch (IssueNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
