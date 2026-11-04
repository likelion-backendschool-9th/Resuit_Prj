package com.newlecture.resuit.service;

public interface CommonResumeService {

    public static class UnauthenticatedUserException extends RuntimeException {}
    public static class ResumeNotFoundException extends RuntimeException {}
    public static class ResumeForbiddenException extends RuntimeException {}
    public static class ResumeInUseException extends RuntimeException {}

    public static class InvalidResumeException extends RuntimeException {
        public InvalidResumeException(String message) {
            super(message);
        }
    }
}
