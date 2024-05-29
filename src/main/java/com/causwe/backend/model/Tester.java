package com.causwe.backend.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TESTER")
@AttributeOverride(name = "role", column = @Column(name = "role", insertable = false, updatable = false))
public class Tester extends User {
    public Tester() {
        setRole(Role.TESTER);
    }

    @Override
    public void updateIssue(Issue issue, Issue updatedIssue) {
        if (issue.getReporter().getId().equals(this.getId())) {
            issue.setTitle(updatedIssue.getTitle() != null ? updatedIssue.getTitle() : issue.getTitle());
            issue.setDescription(updatedIssue.getDescription() != null ? updatedIssue.getDescription() : issue.getDescription());
            issue.setPriority(updatedIssue.getPriority() != null ? updatedIssue.getPriority() : issue.getPriority());

            if (updatedIssue.getStatus() == Issue.Status.RESOLVED) {
                issue.setStatus(Issue.Status.RESOLVED);
            }
        }
    }

    @Override
    public boolean canCreateIssue() {
        return true;
    }
}