package info.developerblog.spring.cloud.marathon.discovery.sse;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

/**
 * https://mesosphere.github.io/marathon/docs/event-bus.html#filtering-the-event-stream
 *
 * {
 *   "eventType": "status_update_event",
 *   "timestamp": "2014-03-01T23:29:30.158Z",
 *   "slaveId": "20140909-054127-177048842-5050-1494-0",
 *   "taskId": "my-app_0-1396592784349",
 *   "taskStatus": "TASK_RUNNING",
 *   "appId": "/my-app",
 *   "host": "slave-1234.acme.org",
 *   "ports": [31372],
 *   "version": "2014-04-04T06:26:23.051Z"
 * }
 */

@Data
class StatusUpdateEvent {

    String eventType;
    OffsetDateTime timestamp;
    String slaveId;
    String taskId;
    TaskStatus taskStatus;
    String appId;
    String host;
    Set<Integer> ports;
    String version;

}
