package com.taskmanagerconsole.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.taskmanagerconsole.model.*;
import com.taskmanagerconsole.repository.TaskRepository;

public class TaskService {
    private final TaskRepository repository;
    private final List<Task> tasksCache;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
        this.tasksCache = new ArrayList<>(repository.loadAll());
    }

    private LocalDateTime calculateNextDueDate(LocalDateTime currentDueDate, RepeatInterval repeatInterval, List<java.time.DayOfWeek> repeatDays) {
        if (repeatInterval == null || currentDueDate == null) {
            return currentDueDate;
        }

        return switch (repeatInterval) {
            case DAILY -> currentDueDate.plusDays(1);
            
            case WEEKLY -> {
                if (repeatDays != null && !repeatDays.isEmpty()) {
                    LocalDateTime nextDate = currentDueDate.plusDays(1);
                    for (int i = 0; i < 7; i++) {
                        if (repeatDays.contains(nextDate.getDayOfWeek())) {
                            yield nextDate;
                        }
                        nextDate = nextDate.plusDays(1);
                    }
                }
                yield currentDueDate.plusWeeks(1);
            }
            
            case MONTHLY -> currentDueDate.plusMonths(1);
            case YEARLY -> currentDueDate.plusYears(1);
            
            default -> currentDueDate;
        };
    }

    public Task addTask(String title, String description,
         Priority priority, String project, List<String> tags, 
         LocalDateTime dueDate, RepeatInterval repeatInterval, List<java.time.DayOfWeek> repeatDays) {

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Заголовок задачи не может быть пустым");
        }

        int id = generateUniqueId();
    
        Task task = Task.builder()
            .id(id)
            .title(title)
            .description(description)
            .status(TaskStatus.TODO)
            .priority(priority != null ? priority : Priority.MEDIUM)
            .project(project)
            .tags(tags)
            .createdAt(LocalDateTime.now())
            .dueDate(dueDate)
            .repeatInterval(repeatInterval != null ? repeatInterval : RepeatInterval.NONE)
            .repeatDays(repeatDays)
            .completedAt(null)
            .build();

        repository.save(task);
        tasksCache.add(task);
        return task;
    }

    public Task addTask(String title, String description,
         Priority priority, String project, List<String> tags, 
         LocalDateTime dueDate, RepeatInterval repeatInterval) {
        return addTask(title, description, priority, project, tags, dueDate, repeatInterval, null);
    }

    public int generateUniqueId() {
        return tasksCache.stream()
            .mapToInt(Task::getId)
            .max()
            .orElse(0) + 1;        
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasksCache);
    }

    public void deleteTask(int id) {
        boolean removedFromCache = tasksCache.removeIf(task -> task.getId() == id);
        
        List<Task> allTasks = repository.loadAll();
        boolean removedFromRepo = allTasks.removeIf(task -> task.getId() == id);

        if (!removedFromCache && !removedFromRepo) {
            throw new IllegalArgumentException("Задача с ID " + id + " не найдена");
        }
        
        repository.saveAll(allTasks);
    }

    public void deleteAll() {
        repository.deleteAll();
        tasksCache.clear();
    }

    public Optional<Task> getTaskById(int id) {
        return tasksCache.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .or(() -> repository.findById(id));
    }

    public void updateTaskStatus(int id, TaskStatus newStatus) {
        Task task = tasksCache.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);

        if (task == null) {
            task = repository.findById(id).orElseThrow(() -> 
                new IllegalArgumentException("Задача с ID " + id + " не найдена")
            );
            tasksCache.add(task);
        }

        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
            if (task.getRepeatInterval() != null && task.getRepeatInterval() != RepeatInterval.NONE && task.getDueDate() != null) {
                LocalDateTime nextDueDate = calculateNextDueDate(task.getDueDate(), task.getRepeatInterval(), task.getRepeatDays());
                
                // Исправлено: добавлена запятая и передача дней недели
                addTask(
                    task.getTitle(),
                    task.getDescription(),
                    task.getPriority(),
                    task.getProject(),
                    task.getTags(),
                    nextDueDate,
                    task.getRepeatInterval(),
                    task.getRepeatDays()
                );
            }
        } else {
            task.setCompletedAt(null);
        }

        repository.save(task);
    }

    public Task updateTask(int id, String title, String description,
         Priority priority, String project, List<String> tags, 
         LocalDateTime dueDate, RepeatInterval repeatInterval, List<java.time.DayOfWeek> repeatDays) {

        Task task = tasksCache.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElse(null);

        if (task == null) {
            task = repository.findById(id).orElseThrow(() -> 
                new IllegalArgumentException("Задача с ID " + id + " не найдена")
            );
            tasksCache.add(task);
        }
        
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setProject(project);
        task.setTags(tags); 
        task.setDueDate(dueDate);
        task.setRepeatInterval(repeatInterval != null ? repeatInterval : RepeatInterval.NONE);
        task.setRepeatDays(repeatDays);

        repository.save(task);
        return task;
    }

    public List<Task> getFilteredTasks(String project, List<String> tags, TaskStatus status, Priority priority, String keyword) {
        return tasksCache.stream()
                .filter(task -> project == null || project.isBlank() || 
                                (task.getProject() != null && task.getProject().equalsIgnoreCase(project)))
                .filter(task -> tags == null || tags.isEmpty() || 
                                (task.getTags() != null && task.getTags().containsAll(tags)))
                .filter(task -> priority == null || task.getPriority() == priority)
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> keyword == null || keyword.isBlank() || 
                                (task.getTitle() != null && task.getTitle().toLowerCase().contains(keyword.toLowerCase())
                                || task.getDescription() != null && task.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }

    public Map<String, Long> getProjectsSummary() {
        return tasksCache.stream()
                .collect(Collectors.groupingBy(
                        task -> (task.getProject() == null || task.getProject().isBlank()) ? "Inbox" : task.getProject(),
                        Collectors.counting()
                ));
    }
}