package andrey.java.task.tracker.api.controllers.helpers;

import andrey.java.task.tracker.api.exceptions.NotFoundException;
import andrey.java.task.tracker.store.entities.ProjectEntity;
import andrey.java.task.tracker.store.entities.TaskStateEntity;
import andrey.java.task.tracker.store.repositories.ProjectRepository;
import andrey.java.task.tracker.store.repositories.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class ControllerHelper {

    ProjectRepository projectRepository;
    TaskStateRepository taskStateRepository;

    public ProjectEntity getProjectOrThrowException(Long projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn't exist.",
                                        projectId
                                )
                        )
                );
    }

    public TaskStateEntity getTaskStateEntityOrThrowException(Long taskStateId){

        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Task state with \"%s\" doesn't exist.",
                                        taskStateId
                                )
                        ));
    }
}