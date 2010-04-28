package com.joshlong.jukebox2.services.workflow;

/**
 * Thrown to rollback any workflow
 */
public class WorkflowException extends RuntimeException {
    public WorkflowException() {
        super(); // To change body of overridden methods use File | Settings |
        // File Templates.
    }

    public WorkflowException(String message) {
        super(message); // To change body of overridden methods use File |
        // Settings | File Templates.
    }

    public WorkflowException(String message, Throwable cause) {
        super(message, cause); // To change body of overridden methods use File
        // | Settings | File Templates.
    }

    public WorkflowException(Throwable cause) {
        super(cause); // To change body of overridden methods use File |
        // Settings | File Templates.
    }
}
