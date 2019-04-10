package info.developerblog.spring.cloud.marathon.discovery.sse;

public enum TaskStatus {
    TASK_STAGING,
    TASK_STARTING,
    TASK_RUNNING,
    TASK_FINISHED,
    TASK_FAILED,
    TASK_KILLING,
    TASK_KILLED,
    TASK_LOST
}
