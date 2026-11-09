package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.JobApplication;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
	boolean existsByJobIdAndUserIdAndCanceledAtIsNull(Long jobId, UUID userId);
	Optional<JobApplication> findByJobIdAndUserIdAndCanceledAtIsNull(Long jobId, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from JobApplication a where a.id = :id")
    Optional<JobApplication> findLockedById(@Param("id") Long id);

    List<JobApplication> findByUserIdAndSnapshotExpiresAtAfterAndSnapshotDeletedAtIsNullOrderByAppliedAtDesc(
        UUID userId, LocalDateTime now);

    @Query("select a.id from JobApplication a where a.snapshotExpiresAt <= :now and a.snapshotDeletedAt is null and a.id > :afterId order by a.id")
    List<Long> findExpiredIds(@Param("now") LocalDateTime now,
        @Param("afterId") Long afterId, Pageable page);

    @Modifying
    @Query("update JobApplication a set a.commonResume = null where a.commonResume.id = :id")
    void detachCommon(@Param("id") Long id);

    @Modifying
    @Query("update JobApplication a set a.targetResume = null where a.targetResume.id = :id")
    void detachTarget(@Param("id") Long id);
}
