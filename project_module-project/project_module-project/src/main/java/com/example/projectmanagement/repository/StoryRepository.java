package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoryRepository extends JpaRepository<Story, Long> {
    
    List<Story> findByEpicId(Long epicId);
    
    List<Story> findByStatus(Story.StoryStatus status);
    
    List<Story> findByAssigneeId(Long assigneeId);
    
    List<Story> findByReporterId(Long reporterId);
    List<Story> findBySprintId(Long sprintId);

    
    @Query("SELECT s FROM Story s WHERE s.epic.id = :epicId AND s.status = :status")
    List<Story> findByEpicIdAndStatus(@Param("epicId") Long epicId, @Param("status") Story.StoryStatus status);
    
    @Query("SELECT s FROM Story s WHERE s.epic.id = :epicId")
    Page<Story> findByEpicId(@Param("epicId") Long epicId, Pageable pageable);
    
    @Query("SELECT s FROM Story s WHERE s.title LIKE %:title%")
    Page<Story> findByTitleContaining(@Param("title") String title, Pageable pageable);
    
    @Query("SELECT s FROM Story s WHERE s.priority = :priority")
    Page<Story> findByPriority(@Param("priority") Story.Priority priority, Pageable pageable);
    
long countByStatus(Story.StoryStatus status);

    @Query("SELECT s FROM Story s " +
        "WHERE (:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
        "AND (:priority IS NULL OR s.priority = :priority) " +
        "AND (:epicId IS NULL OR s.epic.id = :epicId) " +
        "AND (:sprintId IS NULL OR s.sprint.id = :sprintId) " +
        "AND (:projectId IS NULL OR s.project.id = :projectId)")
    Page<Story> searchByFilters(
            @Param("title") String title,
            @Param("priority") Story.Priority priority,
            @Param("epicId") Long epicId,
            @Param("sprintId") Long sprintId,
            @Param("projectId") Long projectId,
            Pageable pageable
    );
}