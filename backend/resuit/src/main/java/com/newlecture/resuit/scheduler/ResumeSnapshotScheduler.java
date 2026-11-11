package com.newlecture.resuit.scheduler;

import com.newlecture.resuit.repository.JobApplicationRepository;
import com.newlecture.resuit.service.JobApplicationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeSnapshotScheduler {
    private final JobApplicationRepository applications;
    private final JobApplicationService service;

    // Each row runs in a separate transaction, so a failed file deletion does not roll back other rows.
    @Scheduled(cron = "${resuit.snapshots.cleanup-cron:0 */10 * * * *}")
    public void purgeExpired() {
        long afterId = 0;
        var now = LocalDateTime.now();
        java.util.List<Long> batch;
        do {
            batch = applications.findExpiredIds(now, afterId, PageRequest.of(0, 200));
            for (Long id : batch) {
                try { service.purgeExpiredSnapshot(id); }
                catch (RuntimeException e) { log.error("제출 스냅샷 정리 실패 applicationId={}; 다음 실행에서 재시도", id, e); }
                afterId = id;
            }
        } while (batch.size() == 200);
    }
}
