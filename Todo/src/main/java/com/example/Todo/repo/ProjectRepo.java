package com.example.Todo.repo;

import com.example.Todo.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepo extends MongoRepository<Project, String> {

}
