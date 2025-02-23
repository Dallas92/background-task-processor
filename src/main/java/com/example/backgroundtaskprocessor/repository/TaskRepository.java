package com.example.backgroundtaskprocessor.repository;

import com.example.backgroundtaskprocessor.model.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    Boolean existsByMinAndMaxAndCountAndIsComplete(Integer min, Integer max, Integer count, Boolean isComplete);

    List<TaskEntity> findAllByIsCompleteFalse();
}
