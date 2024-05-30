package com.causwe.backend.exceptions;

public class IssueNotFoundException extends RuntimeException {
    public IssueNotFoundException(Long id) {
        super("Issue not found with ID: " + id);
    }
}