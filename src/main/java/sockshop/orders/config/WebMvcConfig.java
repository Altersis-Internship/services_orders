package sockshop.orders.config;

import io.micrometer.core.instrument.MeterRegistry;
import sockshop.orders.middleware.HTTPMonitoringInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
public class WebMvcConfig {

    @Bean
    public HTTPMonitoringInterceptor httpMonitoringInterceptor(MeterRegistry meterRegistry) {
        return new HTTPMonitoringInterceptor(meterRegistry);
    }

    @Bean
    public MappedInterceptor myMappedInterceptor(HTTPMonitoringInterceptor interceptor) {
        return new MappedInterceptor(new String[]{"/**"}, interceptor);
    }
}
