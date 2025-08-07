package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.CommentDto;
import com.example.projectmanagement.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/task/{taskId}")
    public ResponseEntity<CommentDto> addCommentToTask(
            @PathVariable Long taskId,
            @RequestParam Long userId,
            @RequestBody String content
    ) {
        return ResponseEntity.ok(commentService.addCommentToTask(taskId, userId, content));
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<CommentDto>> getCommentsByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId));
    }

    @PostMapping("/story/{storyId}")
public ResponseEntity<CommentDto> addCommentToStory(
        @PathVariable Long storyId,
        @RequestParam Long userId,
        @RequestBody CommentDto commentDto) {
    CommentDto savedComment = commentService.addCommentToStory(storyId, userId, commentDto);
    return new ResponseEntity<>(savedComment, HttpStatus.CREATED);
}


    @GetMapping("/story/{storyId}")
    public ResponseEntity<List<CommentDto>> getCommentsByStory(@PathVariable Long storyId) {
        return ResponseEntity.ok(commentService.getCommentsByStoryId(storyId));
    }

    @PostMapping("/epic/{epicId}")
    public ResponseEntity<CommentDto> addCommentToEpic(
            @PathVariable Long epicId,
            @RequestParam Long userId,
            @RequestBody String content
    ) {
        return ResponseEntity.ok(commentService.addCommentToEpic(epicId, userId, content));
    }

    @GetMapping("/epic/{epicId}")
    public ResponseEntity<List<CommentDto>> getCommentsByEpic(@PathVariable Long epicId) {
        return ResponseEntity.ok(commentService.getCommentsByEpicId(epicId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
