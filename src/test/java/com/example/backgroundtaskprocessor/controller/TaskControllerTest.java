package com.example.backgroundtaskprocessor.controller;

import com.example.backgroundtaskprocessor.exception.ConcurrentTaskExecutionException;
import com.example.backgroundtaskprocessor.exception.TaskNotFoundException;
import com.example.backgroundtaskprocessor.model.api.CreateTaskRequest;
import com.example.backgroundtaskprocessor.model.api.TaskDto;
import com.example.backgroundtaskprocessor.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void before() {
        reset(taskService);
    }

    @Test
    @SneakyThrows
    void testCreateTask() {
        when(taskService.create(any(), any(), any())).thenReturn(1L);

        var request = new CreateTaskRequest(1, 10, 20);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @SneakyThrows
    void testCreateTaskWhenConflict() {
        var request = new CreateTaskRequest(1, 10, 20);

        when(taskService.create(any(), any(), any()))
                .thenThrow(new ConcurrentTaskExecutionException("Task (min=%s, max=%s, count=%s) is already running"
                        .formatted(request.min(), request.max(), request.count())));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @SneakyThrows
    void testGetCreate() {
        Long id = 1L;
        when(taskService.get(any())).thenReturn(new TaskDto(id, 10, true));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.counter").value(10))
                .andExpect(jsonPath("$.isComplete").value(true));
    }

    @Test
    @SneakyThrows
    void testGetCreateWhenNotFound() {
        Long id = 1L;
        when(taskService.get(any()))
                .thenThrow(new TaskNotFoundException("Task (id=%s) not found".formatted(id)));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks/{id}", id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
