package com.newlecture.resuit.dto.resume;

import com.newlecture.resuit.entity.Job;
import com.newlecture.resuit.entity.JobQuestion;
import java.time.LocalDate;

// 특화 이력서 화면에 함께 보여 줄 공고 정보 (읽기 전용)
public record JobInfo(Long id, String title, String companyName, String type, LocalDate deadline) {

    public record Question(Long id, String content, Integer charLimit, int orderNo) {
        public static Question from(JobQuestion question) {
            return new Question(question.getId(), question.getContent(), question.getCharLimit(), question.getOrderNo());
        }
    }

    public static JobInfo from(Job job) {
        return new JobInfo(job.getId(), job.getTitle(), companyName(job), job.getType(), job.getDeadline());
    }

    static String companyName(Job job) {
        var company = job.getCompany();
        return company.getNickname() == null || company.getNickname().isBlank() ? company.getUsername() : company.getNickname();
    }
}
