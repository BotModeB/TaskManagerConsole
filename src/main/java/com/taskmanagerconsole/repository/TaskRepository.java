package com.taskmanagerconsole.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.taskmanagerconsole.model.Task;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {
    private final File file = new File("tasks.json");
    private final ObjectMapper objectMapper;

    public TaskRepository(){
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    public void saveAll(List<Task> tasks){
        try{
            objectMapper.writeValue(file, tasks);
        }catch (IOException e){
            System.out.println("Ошибка записи JSON" + e.getMessage());
        }
    }

    public List<Task> loadAll(){
        if (!file.exists()){
            return new ArrayList<>();
        }

        try{
            return objectMapper.readValue(file, new TypeReference<List<Task>>() {});
        }catch (IOException e){
            System.out.println("Ошибка чтения JSON" + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public void save(Task task){
        List<Task> tasks = loadAll();
        tasks.removeIf(t -> t.getId() == task.getId());
        tasks.add(task);
        saveAll(tasks);
    }

    public Optional<Task> findById(int id){
        return loadAll().stream()
            .filter(task -> task.getId() == id)
            .findFirst();
    }

    public void deleteAll(){
        saveAll(new ArrayList<>());
    }

    public boolean deleteById(int id){
        List<Task> tasks = loadAll();
        boolean removed = tasks.removeIf(t -> t.getId() == id);
        if (removed) saveAll(tasks);
        return removed;
    }
}
