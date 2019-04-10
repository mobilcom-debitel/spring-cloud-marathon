package info.developerblog.spring.cloud.marathon.discovery;

import info.developerblog.spring.cloud.marathon.ConditionalOnMarathonEnabled;
import info.developerblog.spring.cloud.marathon.MarathonAutoConfiguration;
import info.developerblog.spring.cloud.marathon.MarathonProperties;
import info.developerblog.spring.cloud.marathon.discovery.sse.MarathonEventStreamDispatcher;
import mesosphere.marathon.client.Marathon;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by aleksandr on 07.07.16.
 */
@Configuration
@ConditionalOnMarathonEnabled
@ConditionalOnProperty(value = "spring.cloud.marathon.discovery.enabled", matchIfMissing = true)
@AutoConfigureAfter(MarathonAutoConfiguration.class)
@EnableConfigurationProperties({MarathonDiscoveryProperties.class, MarathonProperties.class})
public class MarathonDiscoveryClientAutoConfiguration {

    private final Marathon marathonClient;

    public MarathonDiscoveryClientAutoConfiguration(Marathon marathonClient) {
        this.marathonClient = marathonClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public MarathonDiscoveryClient marathonDiscoveryClient(MarathonProperties properties, ApplicationContext context) {
        return new MarathonDiscoveryClient(marathonClient, properties, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public MarathonEventStreamDispatcher marathonEventStreamDispatcher(MarathonProperties properties, ApplicationContext context) {
        return new MarathonEventStreamDispatcher(properties, context);
    }
}
