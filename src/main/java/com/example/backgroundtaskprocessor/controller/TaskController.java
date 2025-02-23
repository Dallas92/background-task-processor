package com.example.backgroundtaskprocessor.controller;

import com.example.backgroundtaskprocessor.model.api.CreateTaskRequest;
import com.example.backgroundtaskprocessor.model.api.CreateTaskResponse;
import com.example.backgroundtaskprocessor.model.api.TaskDto;
import com.example.backgroundtaskprocessor.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping()
    public ResponseEntity<CreateTaskResponse> create(@RequestBody CreateTaskRequest request) {
        Long id = taskService.create(request.min(), request.max(), request.count());
        return ResponseEntity.accepted().body(new CreateTaskResponse(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> get(@PathVariable Long id) {
        TaskDto result = taskService.get(id);
        return ResponseEntity.ok().body(result);
    }
}
