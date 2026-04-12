package com.example.taskflowaryansinha.repository;

import com.example.taskflowaryansinha.entity.Task;
import com.example.taskflowaryansinha.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.status = :status)
              AND (:assigneeId IS NULL OR (t.assignee IS NOT NULL AND t.assignee.id = :assigneeId))
            ORDER BY t.createdAt ASC
            """)
    List<Task> findByProjectWithOptionalFilters(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId
    );

    Optional<Task> findByIdAndProject_Id(UUID id, UUID projectId);

    boolean existsByIdAndProject_Owner_Id(UUID id, UUID ownerId);

    /** Deletes all tasks in a project (e.g. before deleting the project). */
    long deleteByProject_Id(UUID projectId);
}
