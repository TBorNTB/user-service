package com.sejong.userservice.domain.view.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(
        name = "view_daily_history",
        uniqueConstraints = {@UniqueConstraint(name = "uk_date", columnNames = {"date"})}
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewDailyHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    @Column(nullable = false)
    private Long totalViewCount;

    @Column(nullable = false)
    private Long incrementCount;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static ViewDailyHistory of(LocalDate date, Long totalViewCount, Long incrementCount) {
        LocalDateTime now = LocalDateTime.now();
        return ViewDailyHistory.builder()
                .date(date)
                .totalViewCount(totalViewCount)
                .incrementCount(incrementCount)
                .createdAt(now)
                .build();
    }

    public void updateViewCount(Long totalViewCount, Long incrementCount) {
        this.totalViewCount = totalViewCount;
        this.incrementCount = incrementCount;
    }
}

