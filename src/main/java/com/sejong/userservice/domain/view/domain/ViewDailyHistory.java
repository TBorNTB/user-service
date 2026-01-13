package com.sejong.userservice.domain.view.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "view_daily_history",
        uniqueConstraints = {@UniqueConstraint(name = "uk_date", columnNames = {"date"})}
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ViewDailyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private Long totalViewCount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static ViewDailyHistory of(LocalDate date, Long totalViewCount) {
        return ViewDailyHistory.builder()
                .date(date)
                .totalViewCount(totalViewCount)
                .build();
    }

    public void updateTotalViewCount(Long totalViewCount) {
        this.totalViewCount = totalViewCount;
    }
}

