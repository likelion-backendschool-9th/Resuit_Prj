package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.Job;
import com.newlecture.resuit.entity.JobQuestion;
import com.newlecture.resuit.entity.TargetResume;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TargetResumeRepository extends JpaRepository<TargetResume, Long> {

    @Query("""
        SELECT t FROM TargetResume t JOIN FETCH t.job j JOIN FETCH j.company
        WHERE t.user.id = :userId ORDER BY t.id DESC
        """)
    List<TargetResume> findMineWithJob(@Param("userId") UUID userId);

    boolean existsByUserIdAndJobId(UUID userId, Long jobId);

    // 공고·문항 조회. JobRepository는 공고 담당 영역이라 만들지 않고 여기서 읽기만 한다.
    @Query("SELECT j FROM Job j JOIN FETCH j.company WHERE j.id = :jobId")
    Optional<Job> findJob(@Param("jobId") Long jobId);

    @Query("SELECT q FROM JobQuestion q WHERE q.job.id = :jobId ORDER BY q.orderNo")
    List<JobQuestion> findQuestions(@Param("jobId") Long jobId);

    @Query("""
        SELECT q.job.id AS jobId, COUNT(q) AS count FROM JobQuestion q
        WHERE q.job.id IN :jobIds GROUP BY q.job.id
        """)
    List<QuestionCountRow> countQuestions(@Param("jobIds") Collection<Long> jobIds);

    // 지원(2.5)이 APPLIED 상태로 남아 있는 특화 이력서 = 제출 완료
    @Query("""
        SELECT a.targetResume.id FROM JobApplication a
        WHERE a.user.id = :userId AND a.targetResume IS NOT NULL AND UPPER(a.status) = 'APPLIED'
        """)
    List<Long> findSubmittedIds(@Param("userId") UUID userId);

    interface QuestionCountRow {
        Long getJobId();
        long getCount();
    }
}
