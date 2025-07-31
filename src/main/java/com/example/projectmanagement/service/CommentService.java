package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.CommentDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final StoryRepository storyRepository;
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;

    public CommentDto addCommentToTask(Long taskId, Long userId, String content) {
        Task task = getTaskById(taskId);
        User user = getUserById(userId);
        Comment comment = buildComment(user, content);
        comment.setTask(task);
        return mapToDto(commentRepository.save(comment));
    }

    public CommentDto addCommentToStory(Long storyId, Long userId, CommentDto commentDto) {
        Story story = getStoryById(storyId);
        User user = getUserById(userId);
        Comment comment = buildComment(user, commentDto.getText());

        comment.setStory(story);
        return mapToDto(commentRepository.save(comment));
    }

    public CommentDto addCommentToEpic(Long epicId, Long userId, String content) {
        Epic epic = getEpicById(epicId);
        User user = getUserById(userId);
        Comment comment = buildComment(user, content);
        comment.setEpic(epic);
        return mapToDto(commentRepository.save(comment));
    }

    public List<CommentDto> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskId(taskId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByStoryId(Long storyId) {
        return commentRepository.findByStoryId(storyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsByEpicId(Long epicId) {
        return commentRepository.findByEpicId(epicId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: cannot delete comment by another user");
        }

        commentRepository.delete(comment);
    }

    private Comment buildComment(User user, String content) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    private Task getTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
    }

    private Story getStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with ID: " + storyId));
    }

    private Epic getEpicById(Long epicId) {
        return epicRepository.findById(epicId)
                .orElseThrow(() -> new RuntimeException("Epic not found with ID: " + epicId));
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    private CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUserName(comment.getUser().getName());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
