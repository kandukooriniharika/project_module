package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Sprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    
    List<Sprint> findByProjectId(Long projectId);
    
    List<Sprint> findByStatus(Sprint.SprintStatus status);
    
    
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId AND s.status = :status")
    List<Sprint> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") Sprint.SprintStatus status);
    
    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId")
    Page<Sprint> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);
    
    @Query("SELECT s FROM Sprint s WHERE s.startDate <= :date AND s.endDate >= :date")
    List<Sprint> findActiveSprintsOnDate(@Param("date") LocalDateTime date);
    
    @Query("SELECT s FROM Sprint s WHERE s.endDate < :date AND s.status != 'COMPLETED'")
    List<Sprint> findOverdueSprints(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Sprint s WHERE s.project.id = :projectId " +
       "AND (:startDate <= s.endDate AND :endDate >= s.startDate)")
    List<Sprint> findOverlappingSprints(
        @Param("projectId") Long projectId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    @Query("SELECT COUNT(s) > 0 FROM Sprint s WHERE s.project.id = :projectId AND s.status = 'ACTIVE'")
boolean existsActiveSprintInProject(@Param("projectId") Long projectId);


}