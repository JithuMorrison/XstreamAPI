package com.example.Todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Todo.repo.TaskRepo;
import com.example.Todo.repo.UserRepo;
import com.example.Todo.model.Task;
import com.example.Todo.model.User;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@SpringBootApplication
@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class TodoApplication {

	@Autowired
	private TaskRepo taskRepo;

	@Autowired
	private UserRepo userRepo;

	public static void main(String[] args) {
		SpringApplication.run(TodoApplication.class, args);
	}

	@GetMapping("/")
	public String home() {
		return "Hello User! Welcome to this Application";
	}

	@GetMapping("/hello")
	public List<Task> getTasks() {
		return taskRepo.findAll();
	}

	@PostMapping("/addTask")
	public Task addTask(@RequestBody Task task) {
		task.setId(null);
		return taskRepo.save(task);
	}

	@PutMapping("/update/{id}")
	public Task putMethodName(@PathVariable String id, @RequestBody Task entity) {
		return taskRepo.findById(id).map(task -> {
			task.setName(entity.getName());
			task.setDescription(entity.getDescription());
			task.setStatus(entity.getStatus());
			taskRepo.save(task);
			return entity;
		}).orElse(entity);
	}

	@DeleteMapping("/delete/{id}")
	public Task deleteTask(@PathVariable String id) {
		taskRepo.deleteById(id);
		return new Task();
	}

	@PostMapping("/login")
	public boolean login(@RequestBody Map<String, String> body) {
		String param = body.get("param");
		String password = body.get("password");

		Optional<User> user = userRepo.findByUsername(param);
		return user.map(u -> u.getPassword().equals(password)).orElse(false);
	}
}
