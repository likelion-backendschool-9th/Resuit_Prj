package com.newlecture.resuit.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.newlecture.resuit.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByCompany_IdAndStatusAndDeletedAtIsNullOrderByUpdatedAtDescIdDesc(
            UUID companyId, String status, Pageable pageable);
    Optional<Job> findByIdAndCompany_IdAndStatusAndDeletedAtIsNull(Long id, UUID companyId, String status);
    @Query("""
            select j from Job j
            where j.deletedAt is null
              and upper(j.status) in ('OPEN', 'CLOSED')
              and (:type is null or upper(j.type) = upper(:type))
            order by j.createdAt desc, j.id desc
            """)
    Page<Job> findPublicJobs(@Param("type") String type, Pageable pageable);

    @Query("""
            select j from Job j
            where j.id = :id and j.deletedAt is null
              and upper(j.status) in ('OPEN', 'CLOSED')
            """)
    Optional<Job> findPublicJobById(@Param("id") Long id);

	Optional<Job> findByIdAndDeletedAtIsNull(Long id);
}
