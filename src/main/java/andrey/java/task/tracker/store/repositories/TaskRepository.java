package andrey.java.task.tracker.store.repositories;

import andrey.java.task.tracker.store.entities.TaskEntity;
import andrey.java.task.tracker.store.entities.TaskStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskStateEntity> findTaskEntityByTaskStateIdAndNameContainsIgnoreCase(
            Long taskStateId,
            String taskName);
}
