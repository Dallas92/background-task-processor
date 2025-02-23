package com.example.backgroundtaskprocessor.service;

import com.example.backgroundtaskprocessor.exception.ConcurrentTaskExecutionException;
import com.example.backgroundtaskprocessor.exception.TaskNotFoundException;
import com.example.backgroundtaskprocessor.model.api.TaskDto;
import com.example.backgroundtaskprocessor.model.entity.TaskEntity;
import com.example.backgroundtaskprocessor.repository.TaskRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void beforeEach() {
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
        assertEquals(1, task.id());
        assertEquals(0, task.counter());
        assertEquals(false, task.isComplete());
    }

    @Test
    void testGetWhenNotFound() {
        Long id = 1L;

        Throwable exception = assertThrows(TaskNotFoundException.class, () -> taskService.get(id));
        assertEquals("Task (id=1) not found", exception.getMessage());
    }
}
