package andrey.java.task.tracker.api.controllers;

import andrey.java.task.tracker.api.controllers.helpers.ControllerHelper;
import andrey.java.task.tracker.api.dto.AckDto;
import andrey.java.task.tracker.api.dto.TaskDto;
import andrey.java.task.tracker.api.exceptions.BadRequestException;
import andrey.java.task.tracker.api.exceptions.NotFoundException;
import andrey.java.task.tracker.api.factories.TaskDtoFactory;
import andrey.java.task.tracker.store.entities.TaskEntity;
import andrey.java.task.tracker.store.entities.TaskStateEntity;
import andrey.java.task.tracker.store.repositories.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@RestController
public class TaskController {

    TaskRepository taskRepository;

    TaskDtoFactory taskDtoFactory;

    ControllerHelper controllerHelper;

    public static final String ADD_TASK = "/api/task_states/{task_state_id}/tasks";
    public static final String GET_TASKS = "/api/task_states/{task_state_id}/tasks";
    public static final String UPDATE_TASK = "/api/tasks/{task_id}";
    public static final String DELETE_TASK = "/api/tasks/{task_id}";

    @GetMapping(GET_TASKS)
    public List<TaskDto> getTasks(@PathVariable(name = "task_state_id") Long taskStateId){

        TaskStateEntity taskState = controllerHelper.getTaskStateEntityOrThrowException(taskStateId);

        return taskState
                .getTasks()
                .stream()
                .map(taskDtoFactory::makeTaskDto)
                .collect(Collectors.toList());
    }

    @PostMapping(ADD_TASK)
    public TaskDto createTask(
            @PathVariable(name = "task_state_id") Long taskStateId,
            @RequestParam(name = "task_name") String taskName) {

        if (taskName.trim().isEmpty()) {
            throw new BadRequestException("Task name can't be empty.");
        }

        TaskStateEntity taskState = controllerHelper.getTaskStateEntityOrThrowException(taskStateId);

        Optional<TaskEntity> optionalAnotherTask = Optional.empty();

        for (TaskEntity task : taskState.getTasks()) {

            if (task.getName().equalsIgnoreCase(taskName)) {
                throw new BadRequestException(String.format("Task \"%s\" already exists.", taskName));
            }
        }

        TaskEntity task = TaskEntity.builder()
                        .name(taskName)
                        .taskState(taskState)
                        .build();

        final TaskEntity savedTask = taskRepository.saveAndFlush(task);

        return taskDtoFactory.makeTaskDto(savedTask);
    }

    @PatchMapping(UPDATE_TASK)
    public TaskDto updateTask(@PathVariable(name = "task_id") Long taskId,
                              @RequestParam(name = "task_name") String taskName){

        if (taskName.trim().isEmpty()){
            throw new BadRequestException("Task name can't be empty");
        }

        TaskEntity task = getTaskOrThrowException(taskId);

        taskRepository
                .findTaskEntityByTaskStateIdAndNameContainsIgnoreCase(
                        task.getTaskState().getId(),
                        taskName
                )
                .filter(anotherTaskState -> !anotherTaskState.getId().equals(taskId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException(String.format("Task \"%s\" already exists.", taskName));
                });

        task.setName(taskName);

        task = taskRepository.saveAndFlush(task);

        return taskDtoFactory.makeTaskDto(task);
    }

    @DeleteMapping(DELETE_TASK)
    public AckDto deleteTask(@PathVariable(name = "task_id") Long taskId){

        TaskEntity task = getTaskOrThrowException(taskId);

        taskRepository.delete(task);

        return AckDto.builder().answer(true).build();
    }


    private TaskEntity getTaskOrThrowException(Long taskId) {

        return taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Task with \"%s\" id doesn't exist.",
                                        taskId
                                )
                        )
                );
    }
}
