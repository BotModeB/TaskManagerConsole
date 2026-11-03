package com.taskmanagerconsole.repository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.taskmanagerconsole.model.Task;
import com.taskmanagerconsole.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;

public class TaskRepositoryTest {
    TaskRepository repository = new TaskRepository();

    @Test
    public void test_tasks_saveAll_and_loadAll(){
        LocalDate dueDate1 = LocalDate.now().plusMonths(1); 
        LocalDate dueDate2 = LocalDate.now().plusMonths(2);
        LocalDate dueDate3 = LocalDate.now().plusMonths(3);
        LocalDate dueDate4 = LocalDate.now().plusMonths(4);


        Task task1 = new Task(1, "buy milk", dueDate1);
        Task task2 = new Task(2, "create service layer", dueDate2);
        Task task3 = new Task(3, "create ui", dueDate3);
        Task task4 = new Task(4, "test app", dueDate4);

        List<Task> originalTasks = List.of(task1, task2, task3, task4);

        repository.saveAll(originalTasks);
        List<Task> loadedTasks = repository.loadAll();

        assertEquals(originalTasks.size(), loadedTasks.size(), "Size of lists not equals");


        Task originalFirst = originalTasks.get(0);
        Task loadedFirst = loadedTasks.get(0);

        assertEquals(originalFirst.getId(), loadedFirst.getId(), "ID first task not changed!");
        assertEquals(originalFirst.getTitle(), loadedFirst.getTitle(), "Name changed!");
        assertEquals(originalFirst.getDueDate(), loadedFirst.getDueDate(), "Date deadline changed!");
        assertEquals(originalFirst.isCompleted(), loadedFirst.isCompleted(), "Status changed!");


        Task originalLast= originalTasks.get(originalTasks.size()-1);
        Task loadedLast = loadedTasks.get(loadedTasks.size()-1);

        assertEquals(originalLast.getId(), loadedLast.getId(), "ID last task not changed!");
        assertEquals(originalLast.getTitle(), loadedLast.getTitle(), "Name changed!");
        assertEquals(originalLast.getDueDate(), loadedLast.getDueDate(), "Date deadline changed!");
        assertEquals(originalLast.isCompleted(), loadedLast.isCompleted(), "Status changed!");
    }
}   
