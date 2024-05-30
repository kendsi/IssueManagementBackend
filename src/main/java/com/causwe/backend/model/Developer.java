package com.causwe.backend.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DEV")
@AttributeOverride(name = "role", column = @Column(name = "role", insertable = false, updatable = false))
public class Developer extends User {
    public Developer() {
        setRole(Role.DEV);
    }

    @Override
    public void updateIssue(Issue issue, Issue updatedIssue) {
        if (updatedIssue.getStatus() == Issue.Status.FIXED) {
            issue.setStatus(Issue.Status.FIXED);
            issue.setFixer(this);
        }
    }

    @Override
    public boolean isDeveloper() {
        return true;
    }
}