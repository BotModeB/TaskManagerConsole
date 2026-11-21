package com.taskmanagerconsole.ui;

import com.taskmanagerconsole.model.Priority;
import com.taskmanagerconsole.model.RepeatInterval;
import com.taskmanagerconsole.model.Task;
import com.taskmanagerconsole.model.TaskStatus;
import com.taskmanagerconsole.service.TaskService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleUI {
    private final TaskService taskService;
    private final Scanner scanner;

    // Глобальный контекст проекта (обязательный выбор на старте или общий Inbox)
    private String activeContextProject = null;

    // Дополнительные локальные фильтры (теги, приоритет, статус, ключевое слово)
    private List<String> activeTagsFilter = null;
    private Priority activePriorityFilter = null;
    private TaskStatus activeStatusFilter = null;
    private String activeKeywordFilter = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleUI(TaskService taskService) {
        this.taskService = taskService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        if (!showStartScreen()) {
            System.out.println("До свидания!");
            return;
        }
        showMainLoop();
    }

    // --- НАЧАЛЬНЫЙ ЭКРАН ---
    private boolean showStartScreen() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("          КОНСОЛЬНЫЙ ТАСК-МЕНЕДЖЕР        ");
            System.out.println("========================================");
            System.out.println();

            Map<String, Long> projectCounts = taskService.getProjectsSummary();

            System.out.println("----------------------------------------");

            if (projectCounts.isEmpty()) {
                System.out.println("Проекты отсутствуют, список задач пуст.");
            } else {
                System.out.println("СПИСОК ПРОЕКТОВ И КОЛИЧЕСТВО ЗАДАЧ:");
                System.out.println("----------------------------------------");
                projectCounts.forEach((project, count) -> {
                    System.out.println(String.format("• %-20s : %d задач(и)", project, count));
                });
            }
            System.out.println("----------------------------------------");

            System.out.println("1. Выбрать проект (обязательный контекст)");
            System.out.println("2. Войти без проекта (все задачи / Inbox)");
            System.out.println("0. Выйти");
            System.out.print("Выберите пункт: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Введите название проекта: ");
                    String proj = scanner.nextLine().trim();
                    if (!proj.isEmpty()) {
                        activeContextProject = proj;
                        System.out.println("Проект-контекст установлен: '" + activeContextProject + "'");
                        return true;
                    } else {
                        System.out.println("Ошибка: Название проекта не может быть пустым.");
                    }
                    break;
                case "2":
                    activeContextProject = null;
                    System.out.println("Вход без фиксированного проекта.");
                    return true;
                case "0":
                    return false;
                default:
                    System.out.println("Ошибка: Неверный выбор, попробуйте снова.");
            }
        }
    }

    // --- ГЛАВНЫЙ ЦИКЛ (СПИСОК ЗАДАЧ + КОМАНДЫ) ---
    private void showMainLoop() {
        while (true) {
            printHeader();
            printTaskList();
            printMainMenuCommands();

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    handleAddTask();
                    break;
                case "2":
                    handleUpdateTask();
                    break;
                case "3":
                    handleDeleteTaskMenu();
                    break;
                case "4":
                    handleFilterMenu();
                    break;
                case "5":
                    handleChangeContextProject();
                    break;
                case "0":
                    System.out.println("Выход из программы. До свидания!");
                    return;
                default:
                    System.out.println("Ошибка: Неверная команда, попробуйте снова.");
            }
        }
    }

    private void printHeader() {
        System.out.println("\n----------------------------------------");
        String projInfo = (activeContextProject != null) ? activeContextProject : "Все проекты (без контекста)";
        System.out.println("Проект (Контекст): " + projInfo);
        if (activeTagsFilter != null || activePriorityFilter != null || activeStatusFilter != null || activeKeywordFilter != null) {
            System.out.println("Применены дополнительные фильтры");
        }
        System.out.println("----------------------------------------");
    }

    private void printTaskList() {
        List<Task> tasks = taskService.getFilteredTasks(
                activeContextProject,
                activeTagsFilter,
                activeStatusFilter,
                activePriorityFilter,
                activeKeywordFilter
        );

        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст.");
        } else {
            System.out.println(String.format("%-3s | %-18s | %-10s | %-8s | %-10s | %-28s", "ID", "Название", "Проект", "Статус", "Приоритет", "Повторение / Дни (П В С Ч П С В)"));
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            
            for (Task t : tasks) {
                String statusMark = t.getStatus() == TaskStatus.DONE ? "[X]" : "[ ]";
                String repeatInfo = formatRepeatDisplay(t);

                System.out.println(String.format("%-3d | %-18s | %-10s | %-8s | %-10s | %-28s",
                        t.getId(),
                        t.getTitle(),
                        t.getProject() != null ? t.getProject() : "Inbox",
                        statusMark + " " + t.getStatus(),
                        t.getPriority(),
                        repeatInfo
                ));
            }
        }
        System.out.println("------------------------------------------------------------------------------------------------------------------");
    }

    private String formatRepeatDisplay(Task task) {
        if (task.getRepeatInterval() == null || task.getRepeatInterval() == RepeatInterval.NONE) {
            return "Нет";
        }

        String intervalShort = switch (task.getRepeatInterval()) {
            case DAILY -> "День";
            case WEEKLY -> "Нед";
            case MONTHLY -> "Месяц";
            case YEARLY -> "Год";
            default -> task.getRepeatInterval().name();
        };

        if (task.getRepeatInterval() == RepeatInterval.WEEKLY) {
            List<DayOfWeek> days = task.getRepeatDays();
            boolean hasDays = (days != null && !days.isEmpty());

            String m = (hasDays && days.contains(DayOfWeek.MONDAY)) ? "■" : "·";
            String tu = (hasDays && days.contains(DayOfWeek.TUESDAY)) ? "■" : "·";
            String w = (hasDays && days.contains(DayOfWeek.WEDNESDAY)) ? "■" : "·";
            String th = (hasDays && days.contains(DayOfWeek.THURSDAY)) ? "■" : "·";
            String f = (hasDays && days.contains(DayOfWeek.FRIDAY)) ? "■" : "·";
            String sa = (hasDays && days.contains(DayOfWeek.SATURDAY)) ? "■" : "·";
            String su = (hasDays && days.contains(DayOfWeek.SUNDAY)) ? "■" : "·";

            return String.format("[%s] [%s%s%s%s%s%s%s]", intervalShort, m, tu, w, th, f, sa, su);
        }

        return "[" + intervalShort + "]";
    }

    private void printMainMenuCommands() {
        System.out.println("МЕНЮ КОМАНД:");
        System.out.println("1. Добавить задачу");
        System.out.println("2. Обновить задачу (статус / данные)");
        System.out.println("3. Удалить задачу / все задачи");
        System.out.println("4. Фильтр");
        System.out.println("5. Сменить проект-контекст");
        System.out.println("0. Выход");
        System.out.print("Выберите команду: ");
    }

    // --- СМЕНА ПРОЕКТА-КОНТЕКСТА НА ЛЕТУ ---
    private void handleChangeContextProject() {
        System.out.println("\n--- СМЕНА ПРОЕКТА-КОНТЕКСТА ---");
        System.out.println("Текущий проект: " + (activeContextProject != null ? activeContextProject : "Без проекта (Inbox)"));
        System.out.println("1. Установить / сменить название проекта");
        System.out.println("2. Сбросить проект (перейти ко всем задачам / Inbox)");
        System.out.println("0. Назад");
        System.out.print("Выберите вариант: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Введите новое название проекта: ");
                String proj = scanner.nextLine().trim();
                if (!proj.isEmpty()) {
                    activeContextProject = proj;
                    System.out.println("Проект-контекст успешно изменен на: '" + activeContextProject + "'");
                } else {
                    System.out.println("Ошибка: Название проекта не может быть пустым.");
                }
                break;
            case "2":
                activeContextProject = null;
                System.out.println("Проект-контекст сброшен. Показаны все задачи.");
                break;
            case "0":
                break;
            default:
                System.out.println("Ошибка: Неверный выбор.");
        }
    }

    // --- ЭКРАН И МЕНЮ ФИЛЬТРАЦИИ ---
    private void handleFilterMenu() {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("            МЕНЮ ФИЛЬТРАЦИИ             ");
            System.out.println("========================================");
            System.out.println("Текущие критерии фильтрации:");
            System.out.println(" 1. Проект (зафиксирован контекстом): " + (activeContextProject != null ? activeContextProject : "Не задан (все проекты)"));
            System.out.println(" 2. Теги:     " + (activeTagsFilter != null ? String.join(", ", activeTagsFilter) : "не заданы"));
            System.out.println(" 3. Приоритет:" + (activePriorityFilter != null ? activePriorityFilter : "не задан"));
            System.out.println(" 4. Статус:   " + (activeStatusFilter != null ? activeStatusFilter : "не задан"));
            System.out.println(" 5. Поиск (ключевое слово): " + (activeKeywordFilter != null ? activeKeywordFilter : "не задан"));
            System.out.println("----------------------------------------");
            System.out.println(" 6. Сбросить дополнительные фильтры");
            System.out.println(" 0. Назад к списку задач");
            System.out.print("Выберите пункт для настройки: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.println("\nВнимание: Проект зафиксирован контекстом сессии. Чтобы изменить его, используйте пункт '5. Сменить проект-контекст' в главном меню.");
                    System.out.println("Нажмите Enter, чтобы продолжить...");
                    scanner.nextLine();
                    break;
                case "2":
                    configureFilterField("Теги");
                    break;
                case "3":
                    configureFilterField("Приоритет");
                    break;
                case "4":
                    configureFilterField("Статус");
                    break;
                case "5":
                    configureFilterField("Ключевое слово");
                    break;
                case "6":
                    resetAdditionalFilters();
                    System.out.println("Дополнительные фильтры сброшены.");
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Ошибка: Неверный выбор.");
            }
        }
    }

    private void configureFilterField(String fieldName) {
        while (true) {
            System.out.println("\n----------------------------------------");
            String currentVal = getCurrentFilterValue(fieldName);
            System.out.println("Пункт: " + fieldName);
            System.out.println("Существующий критерий: " + (currentVal != null ? currentVal : "не задан"));
            System.out.println("----------------------------------------");
            System.out.println("1. Изменить / Установить");
            System.out.println("2. Сбросить этот фильтр");
            System.out.println("0. Назад");
            System.out.print("Выберите действие: ");

            String action = scanner.nextLine().trim();
            if (action.equals("1")) {
                setFilterValue(fieldName);
                break;
            } else if (action.equals("2")) {
                clearFilterField(fieldName);
                System.out.println("Фильтр '" + fieldName + "' сброшен.");
                break;
            } else if (action.equals("0")) {
                break;
            } else {
                System.out.println("Ошибка: Неверное действие.");
            }
        }
    }

    private String getCurrentFilterValue(String fieldName) {
        return switch (fieldName) {
            case "Теги" -> activeTagsFilter != null ? String.join(", ", activeTagsFilter) : null;
            case "Приоритет" -> activePriorityFilter != null ? activePriorityFilter.name() : null;
            case "Статус" -> activeStatusFilter != null ? activeStatusFilter.name() : null;
            case "Ключевое слово" -> activeKeywordFilter;
            default -> null;
        };
    }

    private void setFilterValue(String fieldName) {
        System.out.print("Введите новое значение для '" + fieldName + "': ");
        String val = scanner.nextLine().trim();
        if (val.isEmpty()) {
            System.out.println("Предупреждение: Значение пустое, изменения не применены.");
            return;
        }

        switch (fieldName) {
            case "Теги":
                activeTagsFilter = Arrays.stream(val.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                break;
            case "Приоритет":
                try {
                    activePriorityFilter = Priority.valueOf(val.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: Неверный приоритет (доступно: LOW, MEDIUM, HIGH).");
                }
                break;
            case "Статус":
                try {
                    activeStatusFilter = TaskStatus.valueOf(val.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: Неверный статус (доступно: TODO, IN_PROGRESS, DONE).");
                }
                break;
            case "Ключевое слово":
                activeKeywordFilter = val;
                break;
        }
        System.out.println("Критерий успешно обновлен!");
    }

    private void clearFilterField(String fieldName) {
        switch (fieldName) {
            case "Теги" -> activeTagsFilter = null;
            case "Приоритет" -> activePriorityFilter = null;
            case "Статус" -> activeStatusFilter = null;
            case "Ключевое слово" -> activeKeywordFilter = null;
        }
    }

    private void resetAdditionalFilters() {
        activeTagsFilter = null;
        activePriorityFilter = null;
        activeStatusFilter = null;
        activeKeywordFilter = null;
    }

    // --- ДОБАВЛЕНИЕ ЗАДАЧИ ---
    private void handleAddTask() {
        System.out.println("\n--- ДОБАВЛЕНИЕ ЗАДАЧИ ---");
        System.out.print("Введите название: ");
        String title = scanner.nextLine();

        System.out.print("Введите описание: ");
        String desc = scanner.nextLine();

        String proj = activeContextProject != null ? activeContextProject : "Inbox";
        System.out.println("Проект задачи автоматически назначен: " + proj);

        System.out.print("Введите приоритет (LOW, MEDIUM, HIGH) [по умолчанию MEDIUM]: ");
        String prioInput = scanner.nextLine().trim();
        Priority priority = Priority.MEDIUM;
        if (!prioInput.isEmpty()) {
            try {
                priority = Priority.valueOf(prioInput.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        System.out.print("Введите теги через запятую: ");
        String tagsInput = scanner.nextLine();
        List<String> tags = tagsInput.isBlank() ? null :
                Arrays.stream(tagsInput.split(",")).map(String::trim).collect(Collectors.toList());

        System.out.print("Введите дедлайн (YYYY-MM-DD) или пусто: ");
        String dateInput = scanner.nextLine().trim();
        LocalDateTime dueDate = null;
        if (!dateInput.isEmpty()) {
            try {
                dueDate = LocalDate.parse(dateInput, DATE_FORMATTER).atStartOfDay();
            } catch (DateTimeParseException e) {
                System.out.println("Предупреждение: Неверный формат даты, дедлайн не установлен.");
            }
        }

        System.out.print("Введите интервал повторения (NONE, DAILY, WEEKLY, MONTHLY, YEARLY) [по умолчанию NONE]: ");
        String intervalInput = scanner.nextLine().trim();
        RepeatInterval repeatInterval = RepeatInterval.NONE;
        if (!intervalInput.isEmpty()) {
            try {
                repeatInterval = RepeatInterval.valueOf(intervalInput.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        List<DayOfWeek> repeatDays = null;
        if (repeatInterval == RepeatInterval.WEEKLY) {
            System.out.println("\n[Справка по дням недели]:");
            System.out.println("  MONDAY    - Понедельник");
            System.out.println("  TUESDAY   - Вторник");
            System.out.println("  WEDNESDAY - Среда");
            System.out.println("  THURSDAY  - Четверг");
            System.out.println("  FRIDAY    - Пятница");
            System.out.println("  SATURDAY  - Суббота");
            System.out.println("  SUNDAY    - Воскресенье");
            System.out.print("Введите дни недели через запятую (например: MONDAY, WEDNESDAY, FRIDAY) или оставьте пустым: ");
            
            String daysInput = scanner.nextLine().trim();
            if (!daysInput.isEmpty()) {
                try {
                    repeatDays = Arrays.stream(daysInput.split(","))
                            .map(String::trim)
                            .map(String::toUpperCase)
                            .map(DayOfWeek::valueOf)
                            .collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    System.out.println("Предупреждение: Неверный формат дней недели, будут учитываться все дни.");
                }
            }
        }

        try {
            taskService.addTask(title, desc, priority, proj, tags, dueDate, repeatInterval, repeatDays);
            System.out.println("Задача успешно добавлена!");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    // --- ОБНОВЛЕНИЕ ЗАДАЧИ ---
    private void handleUpdateTask() {
        System.out.print("\nВведите ID задачи для обновления: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            var taskOpt = taskService.getTaskById(id);
            if (taskOpt.isEmpty()) {
                System.out.println("Ошибка: Задача с ID " + id + " не найдена.");
                return;
            }
            Task task = taskOpt.get();

            System.out.println("\nЧто вы хотите обновить?");
            System.out.println("1. Изменить статус (TODO -> IN_PROGRESS -> DONE)");
            System.out.println("2. Полное редактирование ВСЕХ полей задачи сразу");
            System.out.println("3. Редактировать конкретное поле по отдельности (выбор из меню)");
            System.out.print("Выберите вариант: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                System.out.println("Текущий статус: " + task.getStatus());
                System.out.print("Введите новый статус (TODO, IN_PROGRESS, DONE): ");
                String statusStr = scanner.nextLine().trim().toUpperCase();
                TaskStatus newStatus = TaskStatus.valueOf(statusStr);
                taskService.updateTaskStatus(id, newStatus);
                System.out.println("Статус успешно изменен!");
            } else if (choice.equals("2")) {
                // Полное редактирование
                System.out.print("Новое название [" + task.getTitle() + "]: ");
                String title = scanner.nextLine();
                if (title.isBlank()) title = task.getTitle();

                System.out.print("Новое описание [" + (task.getDescription() != null ? task.getDescription() : "") + "]: ");
                String desc = scanner.nextLine();

                System.out.print("Новый приоритет (LOW, MEDIUM, HIGH) [" + task.getPriority() + "]: ");
                String prioInput = scanner.nextLine().trim();
                Priority priority = prioInput.isEmpty() ? task.getPriority() : Priority.valueOf(prioInput.toUpperCase());

                System.out.print("Новый проект [" + (task.getProject() != null ? task.getProject() : "Inbox") + "]: ");
                String proj = scanner.nextLine().trim();
                if (proj.isBlank()) proj = task.getProject();

                System.out.print("Новые теги через запятую: ");
                String tagsInput = scanner.nextLine();
                List<String> tags = tagsInput.isBlank() ? task.getTags() :
                        Arrays.stream(tagsInput.split(",")).map(String::trim).collect(Collectors.toList());

                System.out.print("Новый дедлайн (YYYY-MM-DD): ");
                String dateInput = scanner.nextLine().trim();
                LocalDateTime dueDate = dateInput.isBlank() ? task.getDueDate() : LocalDate.parse(dateInput, DATE_FORMATTER).atStartOfDay();

                System.out.print("Новый интервал повторения (NONE, DAILY, WEEKLY, MONTHLY, YEARLY) [" + task.getRepeatInterval() + "]: ");
                String intervalInput = scanner.nextLine().trim();
                RepeatInterval repeatInterval = intervalInput.isBlank() ? task.getRepeatInterval() : RepeatInterval.valueOf(intervalInput.toUpperCase());

                List<DayOfWeek> repeatDays = task.getRepeatDays();
                if (repeatInterval == RepeatInterval.WEEKLY) {
                    System.out.println("\n[Справка по дням недели]: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
                    System.out.print("Новые дни недели через запятую (оставьте пустым чтобы не менять): ");
                    String daysInput = scanner.nextLine().trim();
                    if (!daysInput.isBlank()) {
                        repeatDays = Arrays.stream(daysInput.split(","))
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .map(DayOfWeek::valueOf)
                                .collect(Collectors.toList());
                    }
                }

                taskService.updateTask(id, title, desc, priority, proj, tags, dueDate, repeatInterval, repeatDays);
                System.out.println("Задача полностью обновлена!");

            } else if (choice.equals("3")) {
                // Полевое (по отдельности) редактирование
                handleSingleFieldUpdate(task);
            } else {
                System.out.println("Ошибка: Неверный выбор.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите корректный числовой ID.");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleSingleFieldUpdate(Task task) {
        while (true) {
            System.out.println("\n--- РЕДАКТИРОВАНИЕ ОТДЕЛЬНОГО ПОЛЯ ---");
            System.out.println("1. Название: " + task.getTitle());
            System.out.println("2. Описание: " + (task.getDescription() != null ? task.getDescription() : "нет"));
            System.out.println("3. Приоритет: " + task.getPriority());
            System.out.println("4. Проект: " + (task.getProject() != null ? task.getProject() : "Inbox"));
            System.out.println("5. Дедлайн: " + (task.getDueDate() != null ? task.getDueDate().toLocalDate() : "нет"));
            System.out.println("6. Повторение (Интервал / Дни)");
            System.out.println("0. Завершить редактирование");
            System.out.print("Выберите поле для изменения: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Введите новое название: ");
                    String title = scanner.nextLine().trim();
                    if (!title.isBlank()) task.setTitle(title);
                    break;
                case "2":
                    System.out.print("Введите новое описание: ");
                    task.setDescription(scanner.nextLine().trim());
                    break;
                case "3":
                    System.out.print("Введите приоритет (LOW, MEDIUM, HIGH): ");
                    try {
                        task.setPriority(Priority.valueOf(scanner.nextLine().trim().toUpperCase()));
                    } catch (Exception e) { System.out.println("Неверный приоритет."); }
                    break;
                case "4":
                    System.out.print("Введите проект: ");
                    task.setProject(scanner.nextLine().trim());
                    break;
                case "5":
                    System.out.print("Введите дедлайн (YYYY-MM-DD): ");
                    try {
                        task.setDueDate(LocalDate.parse(scanner.nextLine().trim(), DATE_FORMATTER).atStartOfDay());
                    } catch (Exception e) { System.out.println("Неверный формат даты."); }
                    break;
                case "6":
                    System.out.print("Введите интервал (NONE, DAILY, WEEKLY, MONTHLY, YEARLY): ");
                    try {
                        RepeatInterval ri = RepeatInterval.valueOf(scanner.nextLine().trim().toUpperCase());
                        task.setRepeatInterval(ri);
                        if (ri == RepeatInterval.WEEKLY) {
                            System.out.println("Справка: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
                            System.out.print("Дни недели через запятую: ");
                            String dInput = scanner.nextLine().trim();
                            if (!dInput.isBlank()) {
                                List<DayOfWeek> days = Arrays.stream(dInput.split(","))
                                        .map(String::trim).map(String::toUpperCase).map(DayOfWeek::valueOf)
                                        .collect(Collectors.toList());
                                task.setRepeatDays(days);
                            }
                        }
                    } catch (Exception e) { System.out.println("Ошибка ввода интервала."); }
                    break;
                case "0":
                    taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), 
                            task.getPriority(), task.getProject(), task.getTags(), 
                            task.getDueDate(), task.getRepeatInterval(), task.getRepeatDays());
                    System.out.println("Изменения сохранены!");
                    return;
                default:
                    System.out.println("Неверный выбор.");
            }
        }
    }

    private void handleDeleteTaskMenu() {
        System.out.println("\n--- УДАЛЕНИЕ ---");
        System.out.println("1. Удалить задачу по ID");
        System.out.println("2. Удалить ВСЕ задачи");
        System.out.println("0. Назад");
        System.out.print("Выберите вариант: ");
        String choice = scanner.nextLine().trim();

        if (choice.equals("1")) {
            System.out.print("Введите ID задачи для удаления: ");
            try {
                int id = Integer.parseInt(scanner.nextLine().trim());
                taskService.deleteTask(id);
                System.out.println("Задача успешно удалена.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        } else if (choice.equals("2")) {
            System.out.print("Вы уверены, что хотите удалить ВСЕ задачи? (yes/no): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                taskService.deleteAll();
                System.out.println("Все задачи удалены.");
            }
        }
    }
}