package com.taskmanagerconsole;

import com.taskmanagerconsole.repository.TaskRepository;
import com.taskmanagerconsole.service.TaskService;
import com.taskmanagerconsole.ui.ConsoleUI;

public class Main {
    public static void main(String[] args) {
        TaskRepository repository = new TaskRepository();
        TaskService service = new TaskService(repository);
        ConsoleUI ui = new ConsoleUI(service);

        ui.start();
    }
}
