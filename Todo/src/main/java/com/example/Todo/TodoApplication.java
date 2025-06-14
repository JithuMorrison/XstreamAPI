package com.example.Todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

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
	public List<Task> getTasks(@RequestParam List<String> ids) {
		return taskRepo.findAllById(ids);
	}

	@PostMapping("/addTask")
	public String addTask(@RequestBody Task task) {
		task.setId(null);
		String id = taskRepo.save(task).getId();
		projectRepo.findById(task.getProjectId()).ifPresent(proj -> {
			List<String> tasks = proj.getTasks();
			if (tasks == null) {
				tasks = new ArrayList<>();
			}
			tasks.add(id);
			proj.setTasks(tasks);
			projectRepo.save(proj);
		});
		return id;
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

	@PutMapping("/assignTask/{userId}")
	public User assignTaskToUser(@PathVariable String userId, @RequestBody Map<String, String> request) {
		String taskId = request.get("taskId");
		String projectId = request.get("projectId");

		// Update user
		User updatedUser = userRepo.findById(userId).map(user -> {
			Map<String, List<String>> assignedTasks = user.getAssignedTasks();
			if (assignedTasks == null) {
				assignedTasks = new HashMap<>();
			}

			assignedTasks.computeIfAbsent(projectId, k -> new ArrayList<>()).add(taskId);
			user.setAssignedTasks(assignedTasks);
			return userRepo.save(user);
		}).orElse(null);

		// Update task with assigned member
		taskRepo.findById(taskId).ifPresent(task -> {
			task.setMemberAssigned(userId);
			taskRepo.save(task);
		});

		return updatedUser;
	}

	@DeleteMapping("/delete/{id}")
	public Task deleteTask(@PathVariable String id) {
		String ProjectId = taskRepo.findById(id)
				.map(Task::getProjectId)
				.orElse("");
		Optional<Project> project = projectRepo.findById(ProjectId);
		Project proj = project.get();
		List<String> projects = proj.getTasks();
		if (projects != null && projects.remove(id)) {
			proj.setTasks(projects);
			projectRepo.save(proj);
		}
		taskRepo.deleteById(id);
		return new Task();
	}

	@PostMapping("/addProject")
	public String addProject(@RequestBody Map<String, String> entity) {
		String userId = entity.get("userid");

		Project project = new Project();
		project.setId(null);
		project.setName(entity.get("name"));
		project.setDescription(entity.get("description"));
		project.setStatus("Started");
		project.setTasks(new ArrayList<>());
		project.setMembers(new ArrayList<>());
		project.getMembers().add(userId);

		String id = projectRepo.save(project).getId();

		userRepo.findById(userId).ifPresent(user -> {
			List<String> projects = user.getProjects();
			if (projects == null)
				projects = new ArrayList<>();
			projects.add(id);
			user.setProjects(projects);

			List<String> roles = user.getRole();
			if (roles == null)
				roles = new ArrayList<>();
			roles.add("creator");
			user.setRole(roles);

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
			project.setMembers(entity.getMembers());
			projectRepo.save(project);
			return project;
		}).orElse(null);
	}

	@GetMapping("/getProject")
	public List<Project> getProject(@RequestParam List<String> id) {
		return projectRepo.findAllById(id);
	}

	@DeleteMapping("/deleteProject/{id}")
	public String deleteProject(@PathVariable String id) {
		List<String> memberIds = projectRepo.findById(id)
				.map(Project::getMembers)
				.orElse(Collections.emptyList());

		userRepo.findAllById(memberIds).forEach(user -> {
			List<String> projects = user.getProjects();
			List<String> roles = user.getRole();

			if (projects != null && roles != null) {
				int index = projects.indexOf(id);
				if (index != -1) {
					projects.remove(index);
					if (roles.size() > index) {
						roles.remove(index);
					}
					user.setProjects(projects);
					user.setRole(roles);
					userRepo.save(user);
				}
			}
		});

		taskRepo.deleteByProjectId(id);
		projectRepo.deleteById(id);
		return "Project deleted successfully";
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

	@PostMapping("/getUsernames")
	public List<String> getUsernames(@RequestBody List<String> userIds) {
		return userRepo.findAllById(userIds)
				.stream()
				.map(User::getUsername)
				.collect(Collectors.toList());
	}

	@PostMapping("/joinProject")
	public ResponseEntity<Project> joinProject(@RequestBody Map<String, String> entity) {
		String userId = entity.get("userid");
		String projectId = entity.get("projectid");

		projectRepo.findById(projectId).ifPresent(project -> {
			List<String> members = project.getMembers();
			if (members == null)
				members = new ArrayList<>();
			if (!members.contains(userId)) {
				members.add(userId);
				project.setMembers(members);
				projectRepo.save(project);
			}
		});

		userRepo.findById(userId).ifPresent(user -> {
			List<String> projects = user.getProjects();
			if (projects == null)
				projects = new ArrayList<>();
			if (!projects.contains(projectId)) {
				projects.add(projectId);
				user.setProjects(projects);

				List<String> roles = user.getRole();
				if (roles == null)
					roles = new ArrayList<>();
				roles.add("member");
				user.setRole(roles);

				userRepo.save(user);
			}
		});

		// Return the full updated project
		return projectRepo.findById(projectId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("/updateUser/{id}")
	public String updateuser(@PathVariable String id, @RequestBody User entity) {
		return userRepo.findById(id).map(user -> {
			user.setUsername(entity.getUsername());
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

	@PutMapping("/updateStatus/{id}")
	public String updateUserStatus(@PathVariable String id, @RequestBody User entity) {
		return userRepo.findById(id).map(user -> {
			user.setStatus(entity.getStatus());
			userRepo.save(user);
			return "User status updated successfully";
		}).orElse("User not found");
	}

	@DeleteMapping("/deleteUser/{id}")
	public String deleteUser(@PathVariable String id) {
		userRepo.deleteById(id);
		return "User deleted successfully";
	}

}
