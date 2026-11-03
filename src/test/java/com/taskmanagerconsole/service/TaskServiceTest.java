package com.taskmanagerconsole.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.taskmanagerconsole.model.Task;
import com.taskmanagerconsole.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;

public class TaskServiceTest {

    @Test
    public void test_task_add(){
        TaskRepository repository = new TaskRepository();
        
        Task task1 = new Task(1, "buy milk", LocalDate.now());
        Task task2 = new Task(2, "create service layer", LocalDate.now());
        Task task3 = new Task(3, "create ui", LocalDate.now());
        Task task4 = new Task(4, "test app", LocalDate.now());
        repository.saveAll(List.of(task1, task2, task3, task4));

        TaskService service = new TaskService(repository);

        String originalTitle = "big dick, big ball";
        service.addTask(originalTitle, LocalDate.now());

        List<Task> loadTasks = repository.loadAll();
        Task lastTask = loadTasks.getLast();
        
        assertEquals(originalTitle, lastTask.getTitle(), "task not added");
        assertEquals(5, lastTask.getId(), "generated ID is incorrect");
    }

    @Test
    public void test_get_tasks(){
        TaskRepository repository = new TaskRepository();
        Task task1 = new Task(1, "buy milk", LocalDate.now());
        repository.saveAll(List.of(task1));

        TaskService service = new TaskService(repository);

        List<Task> loadedTasks = service.getAllTasks();
        Task loadedFirstTask = loadedTasks.get(0);

        assertEquals(task1.getId(), loadedFirstTask.getId(), "ID не совпадает");
        assertEquals(task1.getTitle(), loadedFirstTask.getTitle(), "Название не совпадает");
    }

    @Test
    public void test_delete_task(){
        TaskRepository repository = new TaskRepository();
        Task task1 = new Task(1, "buy milk", LocalDate.now());
        Task task2 = new Task(2, "create service layer", LocalDate.now());
        repository.saveAll(List.of(task1, task2));

        TaskService service = new TaskService(repository);

        service.deleteTask(1);
        
        List<Task> loadedTasks = repository.loadAll();
        assertEquals(1, loadedTasks.size(), "Размер списка должен быть 1");
        assertEquals(task2.getId(), loadedTasks.get(0).getId(), "Осталась не та задача");
    }

    @Test
    public void test_toggle_task_status(){
        TaskRepository repository = new TaskRepository();
        Task task1 = new Task(1, "buy milk", LocalDate.now());
        Task task2 = new Task(2, "big dick, big ball", LocalDate.now());   
        repository.saveAll(List.of(task1, task2));

        TaskService service = new TaskService(repository);
        boolean originalStatus = task1.isCompleted();

        service.toggleTaskStatus(1);
        
        Task loadedTask = repository.loadAll().get(0);
        boolean changedStatus = loadedTask.isCompleted();

        assertEquals(!originalStatus, changedStatus, "toggle task status not changed");
    }
}
