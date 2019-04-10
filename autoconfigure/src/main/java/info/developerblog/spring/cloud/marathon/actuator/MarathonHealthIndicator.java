package info.developerblog.spring.cloud.marathon.actuator;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.model.v2.GetServerInfoResponse;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.Collection;

/**
 * Created by aleksandr on 01.08.16.
 */
public class MarathonHealthIndicator extends AbstractHealthIndicator {
    private Marathon client;
    private String group;

    public MarathonHealthIndicator(Marathon client, String group) {
        this.client = client;
        this.group = group;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            GetServerInfoResponse serverInfo = client.getServerInfo();
            Collection<?> apps = (group == null) ? client.getApps().getApps() : client.getGroup(group).getApps();
            builder.up()
                    .withDetail("services", apps)
                    .withDetail("name", serverInfo.getName())
                    .withDetail("leader", serverInfo.getLeader())
                    .withDetail("http_port", serverInfo.getHttp_config().getHttp_port())
                    .withDetail("https_port", serverInfo.getHttp_config().getHttps_port())
                    .withDetail("hostname", serverInfo.getMarathon_config().getHostname())
                    .withDetail("local_port_min", serverInfo.getMarathon_config().getLocal_port_min())
                    .withDetail("local_port_max", serverInfo.getMarathon_config().getLocal_port_max());
        }
        catch (Exception e) {
            builder.down(e);
        }
    }
}
