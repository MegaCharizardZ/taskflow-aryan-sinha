package com.example.taskflowaryansinha.repository;

import com.example.taskflowaryansinha.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByOwner_IdOrderByCreatedAtDesc(UUID ownerId);

    Optional<Project> findByIdAndOwner_Id(UUID id, UUID ownerId);

    /**
     * Projects the user owns, or where the user is assignee on at least one task.
     */
    @Query("""
            SELECT DISTINCT p FROM Project p
            WHERE p.owner.id = :userId
               OR p.id IN (SELECT t.project.id FROM Task t WHERE t.assignee.id = :userId)
            ORDER BY p.createdAt DESC
            """)
    List<Project> findVisibleToUser(@Param("userId") UUID userId);

    /**
     * Owner, or assignee on at least one task in this project (same rule as {@link #findVisibleToUser}).
     */
    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p
            WHERE p.id = :projectId
              AND (p.owner.id = :userId
                   OR EXISTS (SELECT 1 FROM Task t WHERE t.project.id = p.id AND t.assignee.id = :userId))
            """)
    boolean isAccessibleByUser(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
