package com.example.taskflow.repository;

import com.example.taskflow.entity.Task;
import com.example.taskflow.models.AssigneeStatusCount;
import com.example.taskflow.models.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    /// For all methods, can also order by priority/ status
    @Query("""
            SELECT t FROM Task t
            WHERE t.project.id = :projectId
              AND (:status IS NULL OR t.taskStatus = :status)
              AND (:assigneeId IS NULL OR (t.assignee.id = :assigneeId))
            ORDER BY t.updatedAt desc
            """)
    List<Task> findByProjectWithOptionalFilters(
            @Param("projectId") UUID projectId,
            @Param("status") TaskStatus status,
            @Param("assigneeId") UUID assigneeId
    );

    Optional<Task> findByIdAndProject_Id(UUID id, UUID projectId);

    boolean existsByIdAndProject_Owner_Id(UUID id, UUID ownerId);

    boolean existsByIdAndCreatedBy_Id(UUID id, UUID createdById);

    /** Deletes all tasks in a project (e.g. before deleting the project). */
    long deleteByProject_Id(UUID projectId);

    @Query("SELECT t.assignee.id AS assigneeId, t.taskStatus AS status, COUNT(t) AS count FROM Task t " +
            "WHERE t.project.id = :projectId AND t.assignee IS NOT NULL GROUP BY t.assignee.id, t.taskStatus")
    List<AssigneeStatusCount> countByAssigneeAndStatusForProject(@Param("projectId") UUID projectId);
}
