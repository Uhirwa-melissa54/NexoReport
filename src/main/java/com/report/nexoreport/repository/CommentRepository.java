package com.report.nexoreport.repository;

import com.report.nexoreport.comment.Comment;
import com.report.nexoreport.model.Issue;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByIssueOrderByCreatedAtAsc(Issue issue);

    void deleteByIssue(Issue issue);
}
