package info.developerblog.spring.cloud.marathon.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import info.developerblog.spring.cloud.marathon.MarathonProperties;
import mesosphere.marathon.client.MarathonException;
import mesosphere.marathon.client.model.v2.HealthCheckResults;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import info.developerblog.spring.cloud.marathon.utils.ServiceIdConverter;
import lombok.extern.slf4j.Slf4j;
import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import mesosphere.marathon.client.model.v2.GetAppsResponse;
import mesosphere.marathon.client.model.v2.VersionedApp;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by aleksandr on 07.07.16.
 */
@Slf4j
public class MarathonDiscoveryClient implements DiscoveryClient, ApplicationListener<ContextRefreshedEvent> {

    private static final String SPRING_CLOUD_MARATHON_DISCOVERY_CLIENT_DESCRIPTION = "Spring Cloud Marathon Discovery Client";

    private static final String ALL_SERVICES = "*";

    private final Marathon client;
    private final String group;
    private final MarathonProperties properties;
    private final ApplicationContext context;

    public MarathonDiscoveryClient(Marathon client, MarathonProperties properties, ApplicationContext context) {
        this.client = client;
        this.group = properties.getGroup();
        this.properties = properties;
        this.context = context;
    }

    @Override
    public String description() {
        return SPRING_CLOUD_MARATHON_DISCOVERY_CLIENT_DESCRIPTION;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Trigger Spring Discovery Client
        this.context.publishEvent(new InstanceRegisteredEvent<>(this, this.properties));
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {

        try {
            List<ServiceInstance> instances = new ArrayList<>();

            if (!allServices(serviceId)) {
                try {
                    instances.addAll(getInstance(serviceId));
                } catch (MarathonException e) {
                    log.error(e.getMessage(), e);
                }

                if (instances.isEmpty()) {
                    Map<String, String> queryMap = new HashMap<>();
                    queryMap.put("id", ServiceIdConverter.convertToMarathonId(serviceId));

                    instances.addAll(getInstances(queryMap));
                }
            } else {
                instances.addAll(getInstances());
            }

            log.debug("Discovered {} service{}{}", instances.size(), instances.size() == 1 ? "" : "s", allServices(serviceId) ? "" : String.format(" with ids that contain [%s]", serviceId));
            return instances;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<ServiceInstance> getInstances() throws MarathonException {
        return getInstances((Map<String, String>)null);
    }

    private List<ServiceInstance> getInstances(Map<String, String> queryMap) throws MarathonException {
        List<ServiceInstance> instances = new ArrayList<>();

        GetAppsResponse appsResponse = queryMap == null ? client.getApps() : client.getApps(queryMap);

        if (appsResponse != null && appsResponse.getApps() != null) {
            List<VersionedApp> apps = appsResponse.getApps();

            log.debug("Discovered {} service{}{}", apps.size(), apps.size() == 1 ? "" : "s", queryMap == null ? "" : String.format(" with ids that contain [%s]", queryMap.get("id")));

            for (App app : apps) {
                // Fetch data for this specific service id, to collect task information
                GetAppResponse response = client.getApp(app.getId());

                if (response != null && response.getApp() != null) {
                    instances.addAll(extractServiceInstances(response.getApp()));
                }
            }
        }

        return instances;
    }

    private List<ServiceInstance> getInstance(String serviceId) throws MarathonException {
        List<ServiceInstance> instances = new ArrayList<>();

        GetAppResponse response = client.getApp(ServiceIdConverter.convertToMarathonId(serviceId));

        if (response != null && response.getApp() != null) {
            instances.addAll(extractServiceInstances(response.getApp()));
        }

        return instances;
    }

    private boolean allServices(String serviceId) {
        return ALL_SERVICES.equals(serviceId);
    }

    /**
     * Extract instances of a service for a specific marathon application
     *
     * @param app
     * @return
     */
    public List<ServiceInstance> extractServiceInstances(App app) {
        log.debug("Discovered service [{}]", app.getId());

        if (app.getTasks().isEmpty()) {
            return Collections.emptyList();
        }

        return app.getTasks()
                .parallelStream()
                .filter(task -> null == task.getHealthCheckResults() ||
                        task.getHealthCheckResults()
                                .stream()
                                .allMatch(HealthCheckResults::getAlive)
                )
                .map(task -> new DefaultServiceInstance(
                        task.getId(),
                        ServiceIdConverter.convertToServiceId(task.getAppId()),
                        task.getHost(),
                        task.getPorts().stream().findFirst().orElse(0),
                        false
                )).map(serviceInstance -> {
                    if (app.getLabels() != null && !app.getLabels().isEmpty())
                        serviceInstance.getMetadata().putAll(app.getLabels());
                    return serviceInstance;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        if (group == null) {
            return getAllApps();
        } else {
            return getGroupApps();
        }
    }

    private List<String> getAllApps() {
        try {
            return client.getApps()
                    .getApps()
                    .parallelStream()
                    .map(App::getId)
                    .map(ServiceIdConverter::convertToServiceId)
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<String> getGroupApps() {
        try {
            return client.getGroup(group)
                    .getApps()
                    .parallelStream()
                    .map(App::getId)
                    .map(ServiceIdConverter::convertToServiceId)
                    .collect(Collectors.toList());
        } catch (MarathonException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}
