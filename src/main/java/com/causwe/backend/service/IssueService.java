package com.causwe.backend.service;

import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.Issue;
import com.causwe.backend.model.Project;
import com.causwe.backend.model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import com.causwe.backend.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

import java.io.IOException;
import java.lang.System;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    private final OkHttpClient httpClient = new OkHttpClient();

    @PersistenceContext
    private EntityManager entityManager;

    public List<Issue> getAllIssues(Long projectId, Long memberId) {
        return issueRepository.IssuesByProjectAndUser(projectId, memberId);
    }

    public Issue getIssueById(Long id) {
        Optional<Issue> issue = issueRepository.findById(id);
        return issue.orElse(null);
    }

    public Issue createIssue(Long projectId, Issue issueData, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Project project = projectService.getProjectById(projectId);

        Issue issue = new Issue(issueData.getTitle(), issueData.getDescription(), currentUser);
        issue.setProject(project);
        Issue newIssue = issueRepository.save(issue);

        // Embed the issue title using Azure OpenAI and store
        issueRepository.embedIssueTitle(newIssue.getId(), newIssue.getTitle());

        return newIssue;
    }

    public Issue updateIssue(Long id, Issue updatedIssue, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }

        Optional<Issue> existingIssue = issueRepository.findById(id);

        if (existingIssue.isPresent()) {
            Issue issue = existingIssue.get();

            // 사용자 역할 기반해서 필드 업데이트 권한 확인
            switch (currentUser.getRole()) {
                case ADMIN:
                    // 관리자는 모든 필드를 업데이트 할 수 있다
                    if (updatedIssue.getTitle() != null) {
                        issue.setTitle(updatedIssue.getTitle());
                    }
                    if (updatedIssue.getDescription() != null) {
                        issue.setDescription(updatedIssue.getDescription());
                    }
                    if (updatedIssue.getPriority() != null) {
                        issue.setPriority(updatedIssue.getPriority());
                    }
                    if (updatedIssue.getStatus() != null) {
                        issue.setStatus(updatedIssue.getStatus());
                    }
                    if (updatedIssue.getAssignee() != null) {
                        issue.setAssignee(userService.getUserById(updatedIssue.getAssignee().getId()));
                        issue.setStatus(Issue.Status.ASSIGNED);
                    }
                    break;
                case PL:
                    // PL은 priority, status, assignee을 업데이트 할 수 있다.
                    if (updatedIssue.getPriority() != null) {
                        issue.setPriority(updatedIssue.getPriority());
                    }
                    if (updatedIssue.getStatus() != null) {
                        issue.setStatus(updatedIssue.getStatus());
                    }
                    if (updatedIssue.getAssignee() != null) {
                        issue.setAssignee(userService.getUserById(updatedIssue.getAssignee().getId()));
                        issue.setStatus(Issue.Status.ASSIGNED);
                    }
                    break;
                case DEV:
                    // Dev는 status to RESOLVED 그리고 set fixer를 할 수 있다.
                    if (updatedIssue.getStatus() == Issue.Status.RESOLVED) {
                        issue.setStatus(updatedIssue.getStatus());
                        issue.setFixer(currentUser);
                    }
                    break;
                case TESTER:
                    // Tester는 status to REOPENED 할 수 있다.
                    if (updatedIssue.getStatus() == Issue.Status.REOPENED) {
                        issue.setStatus(updatedIssue.getStatus());
                    }
                    break;
            }

            return issueRepository.save(issue);
        } else {
            return null;
        }
    }

    public List<Issue> searchIssues(Long projectId, Issue issue, Long memberId) {
        Project project = projectService.getProjectById(projectId);
        User assignee = issue.getAssignee();
        User reporter = issue.getReporter();
        Issue.Status status = issue.getStatus();

        if (assignee != null) {
            return issueRepository.findByProjectAndAssigneeOrderByIdDesc(project, assignee);
        } else if (reporter != null) {
            return issueRepository.findByProjectAndReporterOrderByIdDesc(project, reporter);
        } else if (status != null) {
            return issueRepository.findByProjectAndStatusOrderByIdDesc(project, status);
        } else {
            return issueRepository.IssuesByProjectAndUser(projectId, memberId);
        }
    }

    public List<Issue> searchIssuesByNL(Long projectId, String userMessage, Long memberId) throws IOException {
        String jsonPayload = "{\n" +
                "  \"messages\": [\n" +
                "    {\n" +
                "      \"role\": \"system\",\n" +
                "      \"content\": \"Given the following SQL tables, your job is to write queries that returns issues in the current project given a user’s request. Use Current user_id only when the user uses the phrase \\\"to me\\\". Write PostgreSQL statements without explanation.\\nFormat)\\n```sql\\n``` \\n\\nCREATE TABLE users (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  username VARCHAR(255) NOT NULL UNIQUE,\\n  password VARCHAR(255) NOT NULL,\\n  role ENUM('ADMIN', 'PL', 'DEV', 'TESTER') NOT NULL\\n);\\n\\n-- Create the \\\"projects\\\" table\\nCREATE TABLE projects (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  name VARCHAR(255) NOT NULL UNIQUE\\n);\\n\\n-- Create the \\\"issues\\\" table\\nCREATE TABLE issues (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  title VARCHAR(255) NOT NULL,\\n  description TEXT NOT NULL,\\n  reporter_id BIGINT NOT NULL,\\n  reported_date DATETIME NOT NULL,\\n  fixer_id BIGINT,\\n  assignee_id BIGINT,\\n  priority ENUM('BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR', 'TRIVIAL') NOT NULL,\\n  status ENUM('NEW', 'ASSIGNED', 'RESOLVED', 'CLOSED', 'REOPENED') NOT NULL,\\n  project_id BIGINT NOT NULL,\\n  FOREIGN KEY (reporter_id) REFERENCES users(id),\\n  FOREIGN KEY (fixer_id) REFERENCES users(id),\\n  FOREIGN KEY (assignee_id) REFERENCES users(id),\\n  FOREIGN KEY (project_id) REFERENCES projects(id)\\n);\\n\\n-- Create the \\\"comments\\\" table\\nCREATE TABLE comments (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  issue_id BIGINT NOT NULL,\\n  user_id BIGINT NOT NULL,\\n  content TEXT NOT NULL,\\n  created_at DATETIME NOT NULL,\\n  FOREIGN KEY (issue_id) REFERENCES issues(id),\\n  FOREIGN KEY (user_id) REFERENCES users(id)\\n);\\n\\n--Info\\nCurrent project_id " + projectId + "\\nCurrent user_id " + memberId + "\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \"" + userMessage + "\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"model\": \"llama3-70b-8192\",\n" +
                "  \"temperature\": 0,\n" +
                "  \"max_tokens\": 1024,\n" +
                "  \"top_p\": 1,\n" +
                "  \"stream\": false,\n" +
                "  \"stop\": null,\n" +
                "  \"seed\": 100\n" +
                "}";
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonPayload, mediaType);
        Request request = new Request.Builder()
                .url("https://api.groq.com/openai/v1/chat/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + System.getenv("GROQ_API_KEY"))
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                JSONArray choices = jsonObject.getJSONArray("choices");
                JSONObject messageObject = choices.getJSONObject(0).getJSONObject("message");
                String content = messageObject.getString("content");
                String sqlQuery = content.replaceAll("^```sql\\n|\\n```$", "");

                if (sqlQuery.trim().toUpperCase().startsWith("SELECT")) {
                    Query query = entityManager.createNativeQuery(sqlQuery, Issue.class);
                    return query.getResultList();
                } else {
                    return new ArrayList<>();
                }
            } else {

                throw new IOException("Unexpected code" + response.code());
            }
        }
    }


    public List<User> getRecommendedAssignees(Long projectId, Long id) {
        Optional<Issue> issue = issueRepository.findById(id);

        if (issue.isPresent()) {
            List<Long> assigneeIds = issueRepository.findRecommendedAssigneesByProjectId(projectId, id);
            List<User> unorderedAssignees = new ArrayList<>();

            for (int i = 0; i < assigneeIds.size(); i++) {
                unorderedAssignees.add(userService.getUserById(assigneeIds.get(i)));
            }

            // ID 목록 순서대로 사용자를 정렬
            return assigneeIds.stream()
                    .map(assigneeId -> unorderedAssignees.stream()
                            .filter(user -> user.getId().equals(assigneeId))
                            .findFirst()
                            .orElse(null))
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }
}
