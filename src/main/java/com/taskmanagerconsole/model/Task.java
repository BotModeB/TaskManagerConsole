package com.taskmanagerconsole.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Геттеры, сеттеры, toString, equals, hashCode
@NoArgsConstructor //Пустой конструктор для JSON
@AllArgsConstructor //Конструктор со всеми полями
@Builder //Для удообного создания
public class Task {
    private int id;

    private String title;
    private String description;

    private TaskStatus status; // Enum: TODO(к выполнению),IN_PROGRESS, DONE, ARCHIVED
    private Priority priority; // Enum: LOW, MEDIUM, HUGH, URGENT

    private String project;
    private List<String> tags;

    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private RepeatInterval repeatInterval;
    private List<java.time.DayOfWeek> repeatDays;
}
