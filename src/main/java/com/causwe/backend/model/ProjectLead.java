package com.causwe.backend.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("PL")
@AttributeOverride(name = "role", column = @Column(name = "role", insertable = false, updatable = false))
public class ProjectLead extends User {
    public ProjectLead() {
        setRole(Role.PL);
    }

    @Override
    public void updateIssue(Issue issue, Issue updatedIssue) {
        if (updatedIssue.getPriority() != null) {
            issue.setPriority(updatedIssue.getPriority());
        }
        if (updatedIssue.getAssignee() != null) {
            issue.setAssignee(updatedIssue.getAssignee());
            issue.setStatus(Issue.Status.ASSIGNED);
        }
        if (updatedIssue.getStatus() != null) {
            issue.setStatus(updatedIssue.getStatus());
        }
    }
}