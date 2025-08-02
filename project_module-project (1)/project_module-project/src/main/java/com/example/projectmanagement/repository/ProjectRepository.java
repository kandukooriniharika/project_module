package com.example.projectmanagement.repository;

import com.example.projectmanagement.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
     Page<Project> findByNameContainingAndStatus(String name, Project.ProjectStatus status, Pageable pageable);
    Optional<Project> findByProjectKey(String projectKey);
    
    List<Project> findByStatus(Project.ProjectStatus status);
    
    @Query("SELECT p FROM Project p WHERE p.owner.id = :ownerId")
    List<Project> findByOwnerId(@Param("ownerId") Long ownerId);
    
    @Query("SELECT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findByMemberId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Project p WHERE p.name LIKE %:name%")
    Page<Project> findByNameContaining(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    Page<Project> findByStatus(@Param("status") Project.ProjectStatus status, Pageable pageable);
    
    boolean existsByProjectKey(String projectKey);
}