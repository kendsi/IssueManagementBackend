package com.causwe.backend.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
@AttributeOverride(name = "role", column = @Column(name = "role", insertable = false, updatable = false))
public class Admin extends User {
    public Admin() {
        setRole(Role.ADMIN);
    }

    @Override
    public void updateIssue(Issue issue, Issue updatedIssue) {
        issue.setTitle(updatedIssue.getTitle() != null ? updatedIssue.getTitle() : issue.getTitle());
        issue.setDescription(updatedIssue.getDescription() != null ? updatedIssue.getDescription() : issue.getDescription());
        issue.setPriority(updatedIssue.getPriority() != null ? updatedIssue.getPriority() : issue.getPriority());
        if (updatedIssue.getAssignee() != null) {
            issue.setAssignee(updatedIssue.getAssignee());
            issue.setStatus(Issue.Status.ASSIGNED);
        }
        issue.setStatus(updatedIssue.getStatus() != null ? updatedIssue.getStatus() : issue.getStatus());
    }

    @Override
    public boolean canCreateUser() {
        return true;
    }

    @Override
    public boolean canCreateProject() {
        return true;
    }

    @Override
    public boolean canDeleteProject() {
        return true;
    }

    @Override
    public boolean canCreateIssue() {
        return true;
    }

    @Override
    public boolean canDeleteComment(Comment comment) {
        return true;
    }
}