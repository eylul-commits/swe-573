package com.thehive.model.entity;

import com.thehive.model.enums.ReportStatus;
import com.thehive.model.enums.ReportType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 20, nullable = false)
    private ReportType reportType = ReportType.USER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_offer_id")
    private Offer reportedOffer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_request_id")
    private Request reportedRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_forum_post_id")
    private ForumPost reportedForumPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_forum_topic_id")
    private ForumTopic reportedForumTopic;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportStatus status = ReportStatus.OPEN;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id")
    private User resolvedBy;
}

