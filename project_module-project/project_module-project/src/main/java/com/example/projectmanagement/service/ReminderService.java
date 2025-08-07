package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.ReminderDto;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReminderService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SprintRepository sprintRepository;

    public ReminderDto getReminders() {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(2);

        long tasksDueSoon = taskRepository.countTasksDueSoon(futureDate);
        long projectsWithoutOwner = projectRepository.countProjectsWithoutOwner();
        long sprintsEndingSoon = sprintRepository.countSprintsEndingSoon(futureDate);

        return new ReminderDto(tasksDueSoon, projectsWithoutOwner, sprintsEndingSoon);
    }
}
