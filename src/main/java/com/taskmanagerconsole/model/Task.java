package com.taskmanagerconsole.model;

import java.time.LocalDate;

public class Task {
    private int id;
    private String title;
    private boolean isCompleted;
    private LocalDate dueDate;

    public Task(){
    }

    public Task(int id, String title, LocalDate dueDate) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.isCompleted = false; 
    }

    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id = id;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public LocalDate getDueDate(){
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate){
        this.dueDate = dueDate;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }


    @Override 
    public String toString(){
        String status = isCompleted ? "[X]" : "[ ]";
        return String.format("%d. %s %s (Сделать до: %s)", id, status, title, dueDate);
    }
}
