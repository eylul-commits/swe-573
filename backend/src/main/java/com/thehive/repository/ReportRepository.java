package com.thehive.repository;

import com.thehive.model.entity.Report;
import com.thehive.model.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Integer> {
    
    List<Report> findByReporterId(Integer reporterId);
    
    List<Report> findByReportedUserId(Integer reportedUserId);
    
    List<Report> findByStatus(ReportStatus status);
}

