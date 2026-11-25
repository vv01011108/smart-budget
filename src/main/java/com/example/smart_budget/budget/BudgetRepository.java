package com.example.smart_budget.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByTargetYearMonth(String targetYearMonth);
}
