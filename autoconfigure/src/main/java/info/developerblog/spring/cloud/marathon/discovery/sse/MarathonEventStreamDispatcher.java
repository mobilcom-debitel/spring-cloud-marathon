package info.developerblog.spring.cloud.marathon.discovery.sse;

import info.developerblog.spring.cloud.marathon.MarathonProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalTime;

@Slf4j
public class MarathonEventStreamDispatcher {

    private final ApplicationContext context;
    private final MarathonProperties properties;

    public MarathonEventStreamDispatcher(MarathonProperties properties, ApplicationContext context) {
        this.context = context;
        this.properties = properties;
        this.subscribe();
    }

    private void handleEvent(ServerSentEvent<StatusUpdateEvent> content) {
        StatusUpdateEvent eventData = content.data();
        log.info("Time: {} - event: name[{}], id [{}], content[{}] ",
                LocalTime.now(), content.event(), content.id(), eventData);
        if (properties.getGroup() == null || (eventData != null && eventData.appId.startsWith(properties.getGroup()))) {
            this.context.publishEvent(new InstanceRegisteredEvent<>(this, eventData));
        }
    }

    private void subscribe() {

        WebClient client = WebClient.create(properties.getEndpoint());
        ParameterizedTypeReference<ServerSentEvent<StatusUpdateEvent>> type
                = new ParameterizedTypeReference<ServerSentEvent<StatusUpdateEvent>>() {};

        Flux<ServerSentEvent<StatusUpdateEvent>> eventStream = client
                .get()
                .uri("/v2/events?event_type=status_update_event")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(type);

        eventStream.subscribe(
                this::handleEvent,
                error -> log.error("Error receiving Status Event from Marathon", error),
                () -> log.warn("Event Stream exists unexpected!"));
    }
}
