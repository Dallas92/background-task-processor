package com.example.backgroundtaskprocessor;

import com.example.backgroundtaskprocessor.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupTaskRunner implements ApplicationRunner {

    private final TaskService taskService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking for incomplete tasks on startup...");
        taskService.processIncompleteTasks();
    }
}
