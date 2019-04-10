//#***************************************************************************
//# mobilcom IT Entwicklung Source File: MarathonAutoConfiguration.java
//# Copyright (c) 1996-2019 by mobilcom-debitel GmbH
//# All rights reserved.
//#***************************************************************************
package info.developerblog.spring.cloud.marathon;

import javax.xml.ws.Endpoint;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import info.developerblog.spring.cloud.marathon.actuator.MarathonEndpoint;
import info.developerblog.spring.cloud.marathon.actuator.MarathonHealthIndicator;

import mesosphere.marathon.client.Marathon;
import mesosphere.marathon.client.RibbonMarathonClient;


/********************************************************************
 * Created by aleksandr on 07.07.16.
 */
@ConditionalOnMarathonEnabled
@Configuration
@EnableConfigurationProperties(MarathonProperties.class)
public class MarathonAutoConfiguration
{
	//~ Methods ----------------------------------------------------------------------------------------------------------------


	@Bean
	@ConditionalOnMissingBean
	public Marathon marathonClient(MarathonProperties properties)
	{
		return new RibbonMarathonClient.Builder(properties.getEndpoint()).withListOfServers(properties.getListOfServers())
			.withToken(properties.getToken())
			.withUsername(properties.getUsername())
			.withPassword(properties.getPassword())
			.build();
	}

	//~ Inner Classes ----------------------------------------------------------------------------------------------------------

	@ConditionalOnClass(HealthIndicator.class)
	@Configuration
	protected static class MarathonHealthConfig
	{
		//~ Methods ------------------------------------------------------------------------------------------------------------

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "spring.cloud.discovery.client.marathon.health-indicator.enabled", matchIfMissing = true)
		public MarathonHealthIndicator marathonHealthIndicator(Marathon client, MarathonProperties properties)
		{
			return new MarathonHealthIndicator(client, properties.getGroup());
		}
	}

	@ConditionalOnClass(Endpoint.class)
	@Configuration
	protected static class MarathonEndpointConfig
	{
		//~ Methods ------------------------------------------------------------------------------------------------------------

		@Bean
		@ConditionalOnMissingBean
		public MarathonEndpoint marathonEndpoint(Marathon client)
		{
			return new MarathonEndpoint(client);
		}
	}
}
