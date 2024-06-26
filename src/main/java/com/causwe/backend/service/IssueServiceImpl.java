package com.causwe.backend.service;

import com.causwe.backend.exceptions.IssueNotFoundException;
import com.causwe.backend.exceptions.UnauthorizedException;
import com.causwe.backend.model.*;
import com.causwe.backend.repository.IssueRepository;

import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class IssueServiceImpl implements IssueService {

    private final IssueRepository issueRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Setter
    @PersistenceContext
    private EntityManager entityManager;

    @Setter
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    public IssueServiceImpl(IssueRepository issueRepository, ProjectService projectService, UserService userService) {
        this.issueRepository = issueRepository;
        this.projectService = projectService;
        this.userService = userService;
    }

    @Override
    public List<Issue> getAllIssues(Long projectId, Long memberId) {
        List<Issue> issues = issueRepository.IssuesByProjectAndUser(projectId, memberId);
        for(Issue issue: issues){
            issue.setDescription(null);
        }
        return issues;
    }

    @Override
    public Issue getIssueById(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new IssueNotFoundException(id));
    }

    @Override
    public Issue createIssue(Long projectId, Issue issueData, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }
        if (!(currentUser.canCreateIssue())) {
            throw new UnauthorizedException("User not authorized to create issue");
        }
        Project project = projectService.getProjectById(projectId);

        Issue issue = new Issue(issueData.getTitle(), issueData.getDescription(), issueData.getPriority(), currentUser);
        issue.setProject(project);
        Issue newIssue = issueRepository.save(issue);

        CompletableFuture.runAsync(() ->
                issueRepository.embedIssueTitle(newIssue.getId(), newIssue.getTitle())
        );

        return newIssue;
    }

    @Override
    public Issue updateIssue(Long id, Issue updatedIssue, Long memberId) {
        User currentUser = userService.getUserById(memberId);
        if (currentUser == null) {
            throw new UnauthorizedException("User not logged in");
        }
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IssueNotFoundException(id));

        Issue originalIssueCopy = new Issue(issue);
        currentUser.updateIssue(issue, updatedIssue);
        if (originalIssueCopy.equals(issue)) {
            throw new UnauthorizedException("Issue not changed");
        }
        if(!Objects.equals(originalIssueCopy.getTitle(), issue.getTitle())||!Objects.equals(originalIssueCopy.getDescription(), issue.getDescription())){
            CompletableFuture.runAsync(() ->
                    issueRepository.embedIssueTitle(issue.getId(), issue.getTitle())
            );
        }
        return issueRepository.save(issue);
    }

    @Override
    public List<Issue> searchIssues(Long projectId, String assigneeUsername, String reporterUsername, Issue.Status status, Long memberId) {
        Project project = projectService.getProjectById(projectId);

        if (assigneeUsername != null) {
            User assignee = userService.getUserByUsername(assigneeUsername);
            return issueRepository.findByProjectAndAssigneeOrderByIdDesc(project, assignee);
        } else if (reporterUsername != null) {
            User reporter = userService.getUserByUsername(reporterUsername);
            return issueRepository.findByProjectAndReporterOrderByIdDesc(project, reporter);
        } else if (status != null) {
            return issueRepository.findByProjectAndStatusOrderByIdDesc(project, status);
        } else {
            return issueRepository.IssuesByProjectAndUser(projectId, memberId);
        }
    }

    @Override
    public List<Issue> searchIssuesByNL(Long projectId, String userMessage, Long memberId) throws IOException {
        String cacheKey = "sqlQuery::" + projectId + "::" + userMessage + "::" + memberId; // Create a cache key
        String cachedSqlQuery = cacheManager.getCache("sqlQueries").get(cacheKey, String.class);

        String sqlQuery;
        if (cachedSqlQuery != null) {
            sqlQuery = cachedSqlQuery; // Use the cached query
        } else {
            String jsonPayload = "{\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"system\",\n" +
                    "      \"content\": \"Given the following SQL tables, your job is to write queries that returns issues in the current project given a user’s request. Use Current user_id only when the user uses the phrase \\\"to me\\\". Write PostgreSQL statements without explanation.\\nFormat)\\n```sql\\nORDER BY reported_date DESC;\\n``` \\n\\nCREATE TABLE users (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  username VARCHAR(255) NOT NULL UNIQUE,\\n  password VARCHAR(255) NOT NULL,\\n  role ENUM('ADMIN', 'PL', 'DEV', 'TESTER') NOT NULL\\n);\\n\\n-- Create the \\\"projects\\\" table\\nCREATE TABLE projects (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  name VARCHAR(255) NOT NULL UNIQUE\\n);\\n\\n-- Create the \\\"issues\\\" table\\nCREATE TABLE issues (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  title VARCHAR(255) NOT NULL,\\n  description TEXT NOT NULL,\\n  reporter_id BIGINT NOT NULL,\\n  reported_date DATETIME NOT NULL,\\n  fixer_id BIGINT,\\n  assignee_id BIGINT,\\n  priority ENUM('BLOCKER', 'CRITICAL', 'MAJOR', 'MINOR', 'TRIVIAL') NOT NULL,\\n  status ENUM('NEW', 'ASSIGNED', 'FIXED', 'RESOLVED', 'CLOSED', 'REOPENED') NOT NULL,\\n  project_id BIGINT NOT NULL,\\n  FOREIGN KEY (reporter_id) REFERENCES users(id),\\n  FOREIGN KEY (fixer_id) REFERENCES users(id),\\n  FOREIGN KEY (assignee_id) REFERENCES users(id),\\n  FOREIGN KEY (project_id) REFERENCES projects(id)\\n);\\n\\n-- Create the \\\"comments\\\" table\\nCREATE TABLE comments (\\n  id BIGINT AUTO_INCREMENT PRIMARY KEY,\\n  issue_id BIGINT NOT NULL,\\n  user_id BIGINT NOT NULL,\\n  content TEXT NOT NULL,\\n  created_at DATETIME NOT NULL,\\n  FOREIGN KEY (issue_id) REFERENCES issues(id),\\n  FOREIGN KEY (user_id) REFERENCES users(id)\\n);\\n\\n--Info\\nCurrent project_id " + projectId + "\\nCurrent user_id " + memberId + "\"\n" +
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
                    JSONObject jsonObject = null;
                    if (response.body() != null) {
                        jsonObject = new JSONObject(response.body().string());
                    }
                    JSONArray choices = jsonObject.getJSONArray("choices");
                    JSONObject messageObject = choices.getJSONObject(0).getJSONObject("message");
                    String content = messageObject.getString("content");
                    sqlQuery = content.replaceAll("^```sql\\n|\\n```$|;", "");
                    System.out.println(sqlQuery);
                    Objects.requireNonNull(cacheManager.getCache("sqlQueries")).put(cacheKey, sqlQuery);
                } else {

                    throw new IOException("Unexpected code" + response.code());
                }
            }
        }
        if (sqlQuery.trim().toUpperCase().startsWith("SELECT")) {
            Query query = entityManager.createNativeQuery(sqlQuery, Issue.class);
            return query.getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public List<User> getRecommendedAssignees(Long id) {
        if (issueRepository.existsById(id)) {
            List<Long> assigneeIds = issueRepository.findRecommendedAssigneesByIssueId(id);
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
            throw new IssueNotFoundException(id);
        }
    }
}