package com.example.backgroundtaskprocessor.service;

import com.example.backgroundtaskprocessor.exception.ConcurrentTaskExecutionException;
import com.example.backgroundtaskprocessor.exception.TaskNotFoundException;
import com.example.backgroundtaskprocessor.model.api.TaskDto;
import com.example.backgroundtaskprocessor.model.entity.TaskEntity;
import com.example.backgroundtaskprocessor.repository.TaskRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void beforeEach() {
        taskService.initExecutorService();
        taskRepository.deleteAll();
    }

    @Test
    void testCreate() {
        Integer min = 1;
        Integer max = 10;
        Integer count = 20;

        Long id = taskService.create(min, max, count);

        Optional<TaskEntity> task = taskRepository.findById(id);
        assertTrue(task.isPresent());
        assertEquals(id, task.get().getId());
        assertEquals(1, task.get().getMin());
        assertEquals(10, task.get().getMax());
        assertEquals(20, task.get().getCount());
        assertEquals(0, task.get().getCounter());
        assertEquals(false, task.get().getIsComplete());
    }

    @Test
    @SneakyThrows
    void testCreateWithDelay() {
        Integer min = 1;
        Integer max = 10;
        Integer count = 2;

        Long id = taskService.create(min, max, count);

        Thread.sleep(Duration.ofSeconds(3).toMillis());
        Optional<TaskEntity> task = taskRepository.findById(id);
        assertTrue(task.isPresent());
        assertEquals(id, task.get().getId());
        assertEquals(1, task.get().getMin());
        assertEquals(10, task.get().getMax());
        assertEquals(2, task.get().getCount());
        assertEquals(2, task.get().getCounter());
        assertEquals(true, task.get().getIsComplete());
    }

    @Test
    void testCreateWhenExists() {
        Integer min = 1;
        Integer max = 10;
        Integer count = 20;

        taskService.create(min, max, count);

        Throwable exception = assertThrows(ConcurrentTaskExecutionException.class, () -> taskService.create(min, max, count));
        assertEquals("Task (min=1, max=10, count=20) is already running", exception.getMessage());
    }

    @Test
    void testGet() {
        Integer min = 1;
        Integer max = 10;
        Integer count = 20;

        Long id = taskService.create(min, max, count);

        TaskDto task = taskService.get(id);
        assertNotNull(task);
        assertEquals(id, task.id());
        assertEquals(0, task.counter());
        assertEquals(false, task.isComplete());
    }

    @Test
    void testGetWhenNotFound() {
        Long id = 1L;

        Throwable exception = assertThrows(TaskNotFoundException.class, () -> taskService.get(id));
        assertEquals("Task (id=1) not found", exception.getMessage());
    }

    @Test
    @SneakyThrows
    void testIncompleteTaskProcessing() {
        TaskEntity task1 = new TaskEntity();
        task1.setMin(1);
        task1.setMax(10);
        task1.setCount(2);
        task1.setCounter(0);
        task1.setIsComplete(false);
        taskRepository.save(task1);

        TaskEntity task2 = new TaskEntity();
        task2.setMin(1);
        task2.setMax(10);
        task2.setCount(2);
        task2.setCounter(2);
        task2.setIsComplete(true);
        taskRepository.save(task2);

        taskService.processIncompleteTasks();

        List<TaskEntity> tasks = taskRepository.findAllByIsCompleteFalse();
        assertEquals(1, tasks.size());

        Thread.sleep(Duration.ofSeconds(3).toMillis());

        tasks = taskRepository.findAllByIsCompleteFalse();
        assertEquals(0, tasks.size());
    }
}
