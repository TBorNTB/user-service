package com.sejong.userservice.domain.view.repository;

import com.sejong.userservice.domain.view.domain.ViewDailyHistory;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViewDailyHistoryRepository extends JpaRepository<ViewDailyHistory, Long> {
    Optional<ViewDailyHistory> findByDate(LocalDate date);


    @Query("SELECT COALESCE(SUM(v.incrementCount), 0) FROM ViewDailyHistory v WHERE v.date >= :startDate AND v.date <= :endDate")
    Long findIncrementCountBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    @Query("SELECT COALESCE(SUM(v.incrementCount), 0) FROM ViewDailyHistory v WHERE v.date >= :startDate")
    Long findIncrementCountSince(@Param("startDate") LocalDate startDate);

    @Query("SELECT v FROM ViewDailyHistory v WHERE v.date = :date")
    Optional<ViewDailyHistory> findByDateQuery(@Param("date") LocalDate date);

    /**
     * 특정 기간의 날짜별 증가량을 날짜 순으로 조회합니다.
     */
    @Query("SELECT v FROM ViewDailyHistory v WHERE v.date >= :startDate AND v.date <= :endDate ORDER BY v.date ASC")
    List<ViewDailyHistory> findByDateBetweenOrderByDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

