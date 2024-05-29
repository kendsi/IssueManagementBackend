package com.causwe.backend.model;

import jakarta.persistence.*;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@JsonIgnoreProperties(value = {"password"}, allowSetters = true)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Assuming Single Table Inheritance
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false) // Make role non-insertable and non-updatable in User
    private Role role;

    public enum Role {
        ADMIN, PL, DEV, TESTER
    }

    public User() {}

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Default implementation. Subclasses can override
    public void updateIssue(Issue issue, Issue updatedIssue) {
        throw new UnsupportedOperationException("Update operation not supported for this role.");
    }

    public boolean canCreateUser() {
        return false;
    }
    public boolean canCreateProject() {
        return false;
    }
    public boolean canDeleteProject() {
        return false;
    }
    public boolean canCreateIssue() {
        return false;
    }
    public boolean isDeveloper() {
        return false;
    }
    public boolean canDeleteComment(Comment comment) {
        return comment.getUser().getId().equals(this.id);
    }
}