package com.example.smart_budget.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 전체 지출 목록을 최신 날짜순으로 조회
    // 같은 날짜에 여러 건은 가장 최근에 등록한 내역이 위로
    List<Transaction> findAllByOrderByTransactionDateDescIdDesc();

    // 특정 월별 지출 조회
    List<Transaction> findAllByTransactionDateBetweenOrderByTransactionDateDescIdDesc(LocalDate startDate, LocalDate endDate);
}
