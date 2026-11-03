package com.taskmanagerconsole.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.taskmanagerconsole.model.Task;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            System.out.println("Error writing file JSON" + e.getMessage());
        }
    }

    public List<Task> loadAll(){
        if (!file.exists()){
            return new ArrayList<>();
        }

        try{
            return objectMapper.readValue(file, new TypeReference<List<Task>>() {});
        }catch (IOException e){
            System.out.println("Error reading file JSON" + e.getMessage());
            return new ArrayList<>();
        }
    }
}
