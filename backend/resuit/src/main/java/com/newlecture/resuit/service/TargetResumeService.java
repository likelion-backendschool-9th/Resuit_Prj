package com.newlecture.resuit.service;

// 로그인·이력서 없음·권한·사용 중·잘못된 값 오류는 CommonResumeService의 예외를 함께 쓴다.
public interface TargetResumeService {

    public static class JobNotFoundException extends RuntimeException {}
    public static class NotSpecialJobException extends RuntimeException {}
    public static class TargetResumeExistsException extends RuntimeException {}
}
