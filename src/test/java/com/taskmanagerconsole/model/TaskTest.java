package com.taskmanagerconsole.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
public class TaskTest {
    @Test
    @DisplayName("Проверка успешного создания задачи через Builder")
    public void test_task_creation_with_builder(){
        LocalDateTime now = LocalDateTime.now();
        
        Task task = Task.builder()
        .id(1)
        .title("Тестовая задача")
        .description("Описание задачи")
        .status(TaskStatus.TODO)
        .priority(Priority.HIGH)
        .project("app")
        .tags(List.of("test","java"))
        .createdAt(now)
        .build();

        assertNotNull(task, "Объект задачи не должен быть null");
        assertEquals(1, task.getId(), "ID задачи не совпадает с ID загруженой задачи");
        assertEquals("Тестовая задача", task.getTitle(), "Заголовок задачи не совпадает с заголовком загруженой задачи");
        assertEquals("Описание задачи", task.getDescription(), "Описание задачи не совпадает с описанием загруженой задачи");
        assertEquals(TaskStatus.TODO, task.getStatus(), "Статус задачи не совпадает с статусом загруженой задачи");
        assertEquals(Priority.HIGH, task.getPriority(), "Приоритет задачи не совпадает с приоритетом загруженой задачи");
        assertEquals("app", task.getProject(), "Проект задачи не совпадает с проектом загруженой задачи");
        assertEquals(List.of("test","java"), task.getTags(), "Теги задачи не совпадают с тегами загруженой задачи");
        assertEquals(now, task.getCreatedAt(), "Даты создания задачи не совпадает с датой загруженой задачи");
    }

    @Test
    @DisplayName("Проверка работы сеттеров Lombok") 
    public void test_update_task_status() {
        Task task = Task.builder()
            .id(1)
            .title("Тестовая задачи")
            .status(TaskStatus.TODO)
            .build();

        task.setStatus(TaskStatus.DONE);

        assertEquals(TaskStatus.DONE, task.getStatus(), "Статус не изменился после сеттера");
    }
}
