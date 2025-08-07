package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.DashboardSummaryDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            EpicRepository epicRepository,
                            SprintRepository sprintRepository,
                            UserRepository userRepository,
                            StoryRepository storyRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.userRepository = userRepository;
        this.storyRepository = storyRepository;
    }

    public DashboardSummaryDto getSummary() {
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long totalEpics = epicRepository.count();
        long totalStories = storyRepository.count();
        long totalUsers = userRepository.count();

        // Task status counts
        Map<Task.TaskStatus, Long> taskStatusMap = new EnumMap<>(Task.TaskStatus.class);
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            taskStatusMap.put(status, (long) taskRepository.findByStatus(status).size());
        }

        // Epic status counts
        Map<Epic.EpicStatus, Long> epicStatusMap = new EnumMap<>(Epic.EpicStatus.class);
        for (Epic.EpicStatus status : Epic.EpicStatus.values()) {
            epicStatusMap.put(status, (long) epicRepository.findByStatus(status).size());
        }

        // Story status counts
        Map<Story.StoryStatus, Long> storyStatusMap = new EnumMap<>(Story.StoryStatus.class);
        for (Story.StoryStatus status : Story.StoryStatus.values()) {
            storyStatusMap.put(status, (long) storyRepository.findByStatus(status).size());
        }

        // Convert enum maps to string-keyed maps for DTO
        Map<String, Long> taskStatusCount = new HashMap<>();
        taskStatusMap.forEach((key, value) -> taskStatusCount.put(key.name(), value));

        Map<String, Long> epicStatusCount = new HashMap<>();
        epicStatusMap.forEach((key, value) -> epicStatusCount.put(key.name(), value));

        Map<String, Long> storyStatusCount = new HashMap<>();
        storyStatusMap.forEach((key, value) -> storyStatusCount.put(key.name(), value));

        return DashboardSummaryDto.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .taskStatusCount(taskStatusCount)
                .totalEpics(totalEpics)
                .epicStatusCount(epicStatusCount)
                .totalStories(totalStories)
                .storyStatusCount(storyStatusCount)
                .totalUsers(totalUsers)
                .build();
    }

    public Map<String, Long> getReminders() {
        Map<String, Long> reminders = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusDays(2);

        // 🔔 Tasks due in next 2 days
        reminders.put("taskDueSoonCount", taskRepository.countByDueDateBetween(now, twoDaysLater));

        // 📝 Tasks in TODO
        reminders.put("todoTaskCount", taskRepository.countByStatus(Task.TaskStatus.TODO));

        // 🚩 Projects with no owner
        reminders.put("unassignedProjectCount", projectRepository.countByOwnerIsNull());

        // 🕒 Sprints ending within next 2 days
        reminders.put("sprintsEndingSoonCount", sprintRepository.countByEndDateBetween(now, twoDaysLater));

        // 📘 Stories in TODO
        reminders.put("todoStoryCount", storyRepository.countByStatus(Story.StoryStatus.TODO));

        return reminders;
    }
}
