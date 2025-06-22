package middleware;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.concurrent.TimeUnit;

public class HTTPMonitoringInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;
    private static final String START_TIME = "startTime";

    public HTTPMonitoringInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.nanoTime());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        Long start = (Long) request.getAttribute(START_TIME);
        if (start != null) {
            long elapsed = System.nanoTime() - start;

            Timer.builder("http_server_requests_custom")
                    .description("Custom HTTP request latency")
                    .tags("method", request.getMethod(),
                          "uri", request.getRequestURI(),
                          "status", String.valueOf(response.getStatus()))
                    .register(meterRegistry)
                    .record(elapsed, TimeUnit.NANOSECONDS);
        }
    }
}
