package com.tipico.repository;

import com.tipico.model.condition.Condition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<Condition, Long> {}
