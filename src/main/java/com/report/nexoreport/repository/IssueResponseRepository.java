package com.report.nexoreport.repository;

import com.report.nexoreport.model.Issue;
import com.report.nexoreport.model.IssueResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueResponseRepository extends JpaRepository<IssueResponse, Long> {
    List<IssueResponse> findByIssueOrderByCreatedAtAsc(Issue issue);
}
