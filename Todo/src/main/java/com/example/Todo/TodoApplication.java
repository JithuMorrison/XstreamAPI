package com.example.Todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.Todo.repo.ProjectRepo;
import com.example.Todo.repo.TaskRepo;
import com.example.Todo.repo.UserRepo;
import com.example.Todo.model.Project;
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

	@Autowired
	private ProjectRepo projectRepo;

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
	public String addTask(@RequestBody Task task) {
		task.setId(null);
		return taskRepo.save(task).getId();
	}

	@PutMapping("/update/{id}")
	public Task updatetask(@PathVariable String id, @RequestBody Task entity) {
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

	@PostMapping("/addProject")
	public String addProject(@RequestBody Map<String, String> entity) {
		Project project = new Project();
		project.setId(null);
		project.setName(entity.get("name"));
		project.setDescription(entity.get("description"));
		project.setStatus("Started");
		project.setTasks(new ArrayList<>());
		String id = projectRepo.save(project).getId();
		userRepo.findById(entity.get("userid")).ifPresent(user -> {
			List<String> projects = user.getProjects();
			if (projects == null) {
				projects = new ArrayList<>();
			}
			projects.add(id);
			user.setProjects(projects);
			userRepo.save(user);
		});
		return id;
	}

	@PutMapping("/updateProject")
	public Project updateProject(@RequestBody Project entity) {
		return projectRepo.findById(entity.getId()).map(project -> {
			project.setName(entity.getName());
			project.setDescription(entity.getDescription());
			project.setStatus(entity.getStatus());
			project.setTasks(entity.getTasks());
			projectRepo.save(project);
			return project;
		}).orElse(null);
	}

	@GetMapping("/getProject")
	public List<Project> getProject(@RequestParam List<String> id) {
		return projectRepo.findAllById(id);
	}

	@PostMapping("/login")
	public Map<String, Object> login(@RequestBody Map<String, String> body) {
		String param = body.get("param");
		String password = body.get("password");

		Map<String, Object> response = new HashMap<>();
		Optional<User> user = userRepo.findByEmail(param);

		if (user.isPresent() && user.get().getPassword().equals(password)) {
			response.put("success", true);
			response.put("user", user.get()); // return full user object
		} else {
			response.put("success", false);
			response.put("user", null);
		}
		return response;
	}

	@PostMapping("/register")
	public User addUser(@RequestBody User user) {
		user.setId(null);
		userRepo.save(user);
		return user;
	}

	@GetMapping("/getUser")
	public Boolean getUser(@RequestParam String email) {
		return userRepo.findByEmail(email).isPresent();
	}

	@PutMapping("/updateUser/{id}")
	public String updateuser(@PathVariable String id, @RequestBody User entity) {
		return userRepo.findById(id).map(user -> {
			user.setUsername(entity.getUsername());
			user.setPassword(entity.getPassword());
			user.setEmail(entity.getEmail());
			user.setRole(entity.getRole());
			user.setFname(entity.getFname());
			user.setLname(entity.getLname());
			user.setPhoneno(entity.getPhoneno());
			user.setAddress(entity.getAddress());
			user.setProjects(entity.getProjects());
			userRepo.save(user);
			return "User updated successfully";
		}).orElse("User not found");
	}

	@DeleteMapping("/deleteUser/{id}")
	public String deleteUser(@PathVariable String id) {
		userRepo.deleteById(id);
		return "User deleted successfully";
	}

}
