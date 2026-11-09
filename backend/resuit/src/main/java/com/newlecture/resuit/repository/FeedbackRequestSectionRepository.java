package com.newlecture.resuit.repository;

import com.newlecture.resuit.entity.FeedbackRequestSection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRequestSectionRepository extends JpaRepository<FeedbackRequestSection, Long> {
    void deleteByRequest_Id(Long requestId);
}
