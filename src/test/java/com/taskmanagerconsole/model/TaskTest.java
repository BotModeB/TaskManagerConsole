package com.taskmanagerconsole.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.taskmanagerconsole.model.Task;
import java.time.LocalDate;
public class TaskTest {
    @Test
    public void test_task_creation(){
        int id = 1;
        String title = "Testing task";
        LocalDate dueDate = LocalDate.now();
        
        Task task = new Task(id, title, dueDate);

        assertEquals(1, task.getId(), "Id must be equal to 1");
        assertEquals("Testing task", task.getTitle(), "Title must match");
        assertEquals(dueDate, task.getDueDate(), "Date must match");
        assertFalse(task.isCompleted(), "New task must be uncompleted");
    }

    @Test
    public void test_toggle_status() {
        Task task = new Task(1, "Купить хлеб", LocalDate.now());

        task.setCompleted(true);

        assertTrue(task.isCompleted(), "Task status should have changed to true");
    }
}
