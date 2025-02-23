package com.example.backgroundtaskprocessor.service;

import com.example.backgroundtaskprocessor.exception.ConcurrentTaskExecutionException;
import com.example.backgroundtaskprocessor.exception.TaskNotFoundException;
import com.example.backgroundtaskprocessor.model.api.TaskDto;
import com.example.backgroundtaskprocessor.model.entity.TaskEntity;
import com.example.backgroundtaskprocessor.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    public ExecutorService executorService;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        initExecutorService();
    }

    public void initExecutorService() {
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    System.err.println("Forcing executor shutdown...");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        executorService = Executors.newFixedThreadPool(10);
    }

    public Long create(Integer min, Integer max, Integer count) {
        if (taskRepository.existsByMinAndMaxAndCountAndIsComplete(min, max, count, false)) {
            throw new ConcurrentTaskExecutionException("Task (min=%s, max=%s, count=%s) is already running"
                    .formatted(min, max, count));
        }

        TaskEntity task = new TaskEntity();
        task.setMin(min);
        task.setMax(max);
        task.setCount(count);
        task.setCounter(0);
        task.setIsComplete(false);
        taskRepository.save(task);

        executorService.submit(() -> executeTask(task, 0));

        return task.getId();
    }

    public TaskDto get(Long id) {
        Optional<TaskEntity> taskOptional = taskRepository.findById(id);
        if (taskOptional.isEmpty()) {
            throw new TaskNotFoundException("Task (id=%s) not found".formatted(id));
        }

        return taskOptional
                .stream()
                .map(t -> new TaskDto(t.getId(), t.getCounter(), t.getIsComplete()))
                .toList()
                .get(0);
    }

    public void processIncompleteTasks() {
        List<TaskEntity> tasks = taskRepository.findAllByIsCompleteFalse();
        if (tasks.isEmpty()) {
            log.info("No incomplete tasks found.");
            return;
        }

        log.info("Found {} incomplete tasks. Restarting them...", tasks.size());

        for (var task : tasks) {
            executorService.submit(() -> executeTask(task, task.getCounter()));
        }
    }

    private void executeTask(TaskEntity task, int startFrom) {
        try {
            for (int i = startFrom; i < task.getCount(); i++) {
                Thread.sleep(Duration.ofSeconds(1).toMillis());
                int random = (int) (Math.random() * task.getMax() + task.getMin());
                log.info("Task (id={}, version={}) execution in process with result {}", task.getId(), task.getVersion(), random);
                task.setCounter(task.getCounter() + 1);
                task = taskRepository.save(task);
            }

            task.setIsComplete(true);
            taskRepository.save(task);
            log.info("Task (id={}, version={}) execution completed", task.getId(), task.getVersion());
        } catch (Exception ex) {
            log.error("Task (id={}, version={}) execution failed", task.getId(), task.getVersion(), ex);
        }
    }
}
