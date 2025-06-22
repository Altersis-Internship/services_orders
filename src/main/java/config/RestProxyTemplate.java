package config;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Component
public class RestProxyTemplate {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${proxy.host:}")
    private String host;

    @Value("${proxy.port:}")
    private String port;

    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        configureProxy(restTemplate);
        return restTemplate;
    }

    @PostConstruct
    public void init() {
        this.restTemplate = restTemplate(); // Initialize the restTemplate
    }

    private void configureProxy(RestTemplate restTemplate) {
        if (host.isEmpty() || port.isEmpty()) {
            return;
        }
        int portNr = -1;
        try {
            portNr = Integer.parseInt(port);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse the proxy port number");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        InetSocketAddress address = new InetSocketAddress(host, portNr);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, address);
        factory.setProxy(proxy);

        restTemplate.setRequestFactory(factory);
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}