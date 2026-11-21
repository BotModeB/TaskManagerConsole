package com.taskmanagerconsole.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.taskmanagerconsole.model.Task;
import com.taskmanagerconsole.model.TaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TaskRepositoryTest {

    private TaskRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new TaskRepository();
        repository.deleteAll();
    }

    @Test
    @DisplayName("Массовое сохранение, загрузка и полная очистка хранилища")
    public void test_tasks_saveAll_loadAll_deleteAll() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime nextWeek = LocalDateTime.now().plusWeeks(1);
 
        Task task1 = Task.builder()
                .id(1)
                .title("Title No.1")
                .status(TaskStatus.TODO)
                .createdAt(now)
                .build();

        Task task2 = Task.builder()
                .id(2)
                .title("Title No.2")
                .status(TaskStatus.TODO)
                .createdAt(tomorrow)
                .build();

        Task task3 = Task.builder()
                .id(3)
                .title("Title No.3")
                .status(TaskStatus.TODO)
                .createdAt(nextWeek)
                .build();

        List<Task> originalTasks = List.of(task1, task2, task3);

        repository.saveAll(originalTasks);
        List<Task> loadedTasks = repository.loadAll();

        assertEquals(originalTasks.size(), loadedTasks.size(), 
                "Количество загруженных задач должно совпадать с количеством сохраненных");

        Task originalFirst = originalTasks.get(0);
        Task loadedFirst = loadedTasks.get(0);

        assertEquals(originalFirst.getId(), loadedFirst.getId(), "ID задачи не совпадает с ID загруженой задачи");
        assertEquals(originalFirst.getTitle(), loadedFirst.getTitle(), "Заголовок задачи не совпадает с заголовком загруженой задачи");
        assertEquals(originalFirst.getStatus(), loadedFirst.getStatus(), "Статус задачи не совпадает с статусом загруженой задачи");
        assertEquals(originalFirst.getCreatedAt(), loadedFirst.getCreatedAt(), "Дата создания задачи не совпадает с датой создания загруженой задачи");

        Task originalLast = originalTasks.get(originalTasks.size() - 1);
        Task loadedLast = loadedTasks.get(loadedTasks.size() - 1);

        assertEquals(originalLast.getId(), loadedLast.getId(), "ID задачи не совпадает с ID загруженой задачи");
        assertEquals(originalLast.getTitle(), loadedLast.getTitle(), "Заголовок задачи не совпадает с заголовком загруженой задачи");
        assertEquals(originalLast.getStatus(), loadedLast.getStatus(), "Статус задачи не совпадает с статусом загруженой задачи");
        assertEquals(originalLast.getCreatedAt(), loadedLast.getCreatedAt(), "Дата создания задачи не совпадает с датой создания загруженой задачи");
   
        repository.deleteAll();
        
        assertTrue(repository.loadAll().isEmpty(), "После вызова deleteAll() список задач в файле должен быть пустым");
    }

    @Test 
    @DisplayName("Точечное сохранение, поиск по ID и удаление конкретной записи")
    public void test_task_save_and_find_and_delete_by_id() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
 
        Task task1 = Task.builder()
                .id(1)
                .title("Title No.1")
                .status(TaskStatus.TODO)
                .createdAt(now)
                .build();

        Task task2 = Task.builder()
                .id(2)
                .title("Title No.2")
                .status(TaskStatus.TODO)
                .createdAt(tomorrow)
                .build();

        repository.save(task1);
        repository.save(task2);

        List<Task> tasks = repository.loadAll();
        assertEquals(2, tasks.size(), "В репозитории должно быть ровно 2 задачи");
        assertEquals(1, tasks.get(0).getId(), "Первой в списке должна быть задача с ID 1");

        Optional<Task> found = repository.findById(2);
        assertFalse(found.isEmpty(), "Задача с ID 2 должна успешно находиться в репозитории");
        assertEquals(task2.getTitle(), found.get().getTitle(), "Заголовок найденной задачи не совпадает с оригиналом");

        repository.deleteById(2);
        tasks = repository.loadAll();

        assertEquals(1, tasks.size(), "После удаления одной задачи должен остаться ровно 1 элемент");
        assertEquals(1, tasks.get(0).getId(), "В репозитории должна остаться именно задача с ID 1");
    }
}