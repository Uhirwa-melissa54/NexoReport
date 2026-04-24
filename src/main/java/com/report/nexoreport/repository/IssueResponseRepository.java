package com.report.nexoreport.repository;

import com.report.nexoreport.model.Issue;
import com.report.nexoreport.model.IssueResponse;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IssueResponseRepository extends JpaRepository<IssueResponse, Long> {
    List<IssueResponse> findByIssueOrderByCreatedAtAsc(Issue issue);

    interface SystemActivityRow {
        LocalDate getDate();

        long getResolvedCount();
    }

    @Query(
            value = """
                    select cast(ir.created_at as date) as date, count(*) as resolvedCount
                    from issue_responses ir
                    where ir.type = 'RESOLVED'
                    group by cast(ir.created_at as date)
                    order by date desc
                    """,
            nativeQuery = true
    )
    List<SystemActivityRow> getResolvedIssuesPerDay();
}
