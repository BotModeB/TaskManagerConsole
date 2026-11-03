package com.taskmanagerconsole.ui;

import com.taskmanagerconsole.service.TaskService;
import java.time.LocalDate;
import java.util.Scanner;

public class ConsoleUI {
    private final TaskService taskService;
    private final Scanner scanner;

    public ConsoleUI(TaskService taskService) {
        this.taskService = taskService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n--- Task Manager Console ---");
            System.out.println("1. View all tasks");
            System.out.println("2. Add task");
            System.out.println("3. Changed of status task ([X] or [ ])");
            System.out.println("4. Delete task");
            System.out.println("5. Delete all tasks");
            System.out.println("0. Exit");
            System.out.print("Select menu item ");

            String input = scanner.nextLine(); 

            if (!input.matches("\\d+")) { 
                System.out.println("Error! Enter number from 0 to 4");
                continue; 
            }

            int choice = Integer.parseInt(input); 

            switch (choice) {
                case 1:
                    showAllTasks();
                    break;
                case 2:
                    handleAddTask();
                    break;
                case 3:
                    handleToggleStatus();
                    break;
                case 4:
                    handleDeleteTask();
                    break;
                case 5:
                    handleDeleteAll();
                    break;
                case 0:
                    running = false;
                    System.out.println("Goog buy!");
                    break;
                default:
                    System.out.println("Mission failed! Try again.");
            }
        }
    }


    private void showAllTasks() {
        System.out.println("\n=== Your List Tasks ===");
        var tasks = taskService.getAllTasks();
        if (tasks.isEmpty()) {
            System.out.println("List tasks is empty.");
        } else {
            tasks.forEach(System.out::println);
        }
    }

    private void handleAddTask() {
        System.out.print("Input title new task: ");
        String title = scanner.nextLine();
        
        LocalDate dueDate = LocalDate.now();

        taskService.addTask(title, dueDate);
        System.out.println("Mission passed! Task added.");
    }

    private void handleToggleStatus() {
        System.out.print("Input ID, to change her status: ");
        int id = scanner.nextInt();
        taskService.toggleTaskStatus(id);
        System.out.println("Status task with ID " + id + " changed.");
    }

    private void handleDeleteTask() {
        System.out.print("Input ID task to delete: ");
        int id = scanner.nextInt();
        taskService.deleteTask(id);
        System.out.println("Task with ID " + id + " ruthlessly deleted.");
    }

    private void handleDeleteAll(){
        System.out.println("Are you sure?[Y/n]");
        String sure = scanner.nextLine().toLowerCase();
        if (sure.equals("yes") || sure.equals("y") || sure.isEmpty()){
            taskService.deleteAll();
            System.out.println("Mission passed! Tasks deleted.");
        } else if (sure.equals("not") || sure.equals("n")) {
            System.out.println("Mission failed! You refused.");
        } else {
            System.out.println("Mission failed! You're egg.");
        }
    }
}
