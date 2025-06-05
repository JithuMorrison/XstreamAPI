package com.example.Todo.repo;

import com.example.Todo.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepo extends MongoRepository<Task, String> {

}
