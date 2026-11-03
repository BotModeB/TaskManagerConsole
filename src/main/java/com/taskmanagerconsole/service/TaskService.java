package com.taskmanagerconsole.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.taskmanagerconsole.model.Task;
import com.taskmanagerconsole.repository.TaskRepository;

public class TaskService {
    private TaskRepository repository = new TaskRepository();
    private final List<Task> tasks;

    public TaskService(TaskRepository repository){
        this.repository = repository;
        this.tasks = repository.loadAll();
    }

    public void addTask(String title, LocalDate dueDate){
        int id = generateUniqueId();
    
        Task newTask = new Task(id, title, dueDate);

        tasks.add(newTask);

        repository.saveAll(tasks);
    }

    public int generateUniqueId(){
        return getAllTasks().stream()
            .mapToInt(Task::getId)
            .max()
            .orElse(0)+1;        
    }

    public List<Task> getAllTasks() {
        return tasks;
    }

    public void deleteTask(int id){
        tasks.removeIf(task -> task.getId() == id);
        repository.saveAll(tasks);
    }

    public void deleteAll(){
        List<Task> new_list = new ArrayList<>();
        repository.saveAll(new_list);
    }

    public void toggleTaskStatus(int id){
        tasks.stream()
        .filter(task -> task.getId() == id)
        .findFirst()
        .ifPresent(task -> task.setCompleted(!task.isCompleted()));

        repository.saveAll(tasks);
    }
}
