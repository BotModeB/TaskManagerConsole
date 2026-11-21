package com.taskmanagerconsole.service;

import com.taskmanagerconsole.model.*;
import com.taskmanagerconsole.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для TaskService")
class TaskServiceTest {

    private TaskRepository repository;
    private TaskService service;

    @BeforeEach
    void setUp() {
        repository = new TaskRepository();
        repository.deleteAll();
        service = new TaskService(repository);
    }

    @Nested
    @DisplayName("Тесты добавления задач (addTask)")
    class AddTaskTests {

        @Test
        @DisplayName("Должно успешно добавлять задачу с генерацией ID и дефолтных полей")
        void test_addTask_successfully() {
            Task task = service.addTask("Title", "Description", Priority.HIGH, "", List.of("test"), null, null);

            assertNotNull(task);
            assertEquals(1, task.getId(), "Первой задаче должен присвоиться ID 1");
            assertEquals("Title", task.getTitle());
            assertEquals(TaskStatus.TODO, task.getStatus());
            assertNotNull(task.getCreatedAt());
        }

        @Test
        @DisplayName("Должно выбрасывать исключение, если заголовок пустой")
        void test_throwException_when_title_isEmpty() {
            assertThrows(IllegalArgumentException.class, () -> {
                service.addTask("", "Description", Priority.MEDIUM, "", List.of(), null, null);
            });
        }
    }

    @Nested
    @DisplayName("Тесты обновления статуса (updateTaskStatus)")
    class UpdateStatusTests {

        @Test
        @DisplayName("Должно менять статус на DONE и проставлять completedAt")
        void test_updateStatus_to_done_and_setCompletedAt() {
            Task created = service.addTask("Title", "Description", Priority.LOW, "", List.of(), null, null);
            
            service.updateTaskStatus(created.getId(), TaskStatus.DONE);

            Task updated = service.getTaskById(created.getId()).get();
            assertEquals(TaskStatus.DONE, updated.getStatus());
            assertNotNull(updated.getCompletedAt(), "Дата завершения должна заполниться автоматически");
        }

        @Test
        @DisplayName("Должно кидать исключение, если задача для обновления не найдена")
        void test_throwException_when_task_not_found() {
            assertThrows(IllegalArgumentException.class, () -> {
                service.updateTaskStatus(999, TaskStatus.IN_PROGRESS);
            });
        }
    }

    @Nested
    @DisplayName("Тесты фильтрации ")
    class FilterTests {

        @Test
        @DisplayName("Должно фильтровать список задач по: проект, теги, статус, приоритет, ключевое слово")
        void test_filter_tasks(){
            Task task1 = service.addTask("Title No.1", "Descriprions", Priority.MEDIUM, "TEST", List.of(), null, null);
            Task task2 = service.addTask("Title No.2", "Descriprions", Priority.MEDIUM, "", List.of("TEST"), null, null);
            Task task3 = service.addTask("Title No.3", "Descriprions", Priority.MEDIUM, "", List.of(), null, null);
            service.updateTaskStatus(task3.getId(), TaskStatus.IN_PROGRESS);

            Task task4 = service.addTask("Title No.4", "Descriprions", Priority.HIGH, "", List.of(), null, null);
            Task task5 = service.addTask("Title No.5", "Keyword", Priority.MEDIUM, "", List.of(), null, null);
            
            assertEquals(
                task1.getTitle(), 
                service.getFilteredTasks(task1.getProject(), null, null, null, null).get(0).getTitle(),
             "Неправильная фильтрация по проекту, заголовки не совпали");
            assertEquals(
                task2.getTitle(), 
                service.getFilteredTasks(null, task2.getTags(), null, null, null).get(0).getTitle(),
             "Неправильная фильтрация по тегам, заголовки не совпали");
            assertEquals(
                task3.getTitle(), 
                service.getFilteredTasks(null, null, task3.getStatus(), null, null).get(0).getTitle(),
             "Неправильная фильтрация по статусу, заголовки не совпали");
            assertEquals(
                task4.getTitle(), 
                service.getFilteredTasks(null, null, null, task4.getPriority(), null).get(0).getTitle(),
             "Неправильная фильтрация по приоритету, заголовки не совпали");
            assertEquals(
                task5.getTitle(), 
                service.getFilteredTasks(null, null, null, null, "keyword").get(0).getTitle(),
             "Неправильная фильтрация по ключевому слову, заголовки не совпали");

        }
    }
}