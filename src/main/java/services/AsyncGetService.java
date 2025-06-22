package services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.RestProxyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;

@Service
public class AsyncGetService {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncGetService.class);
    private final RestTemplate restTemplate;

    @Autowired
    public AsyncGetService(RestProxyTemplate proxyTemplate) {
        this.restTemplate = proxyTemplate.getRestTemplate();

        // Optionnel : ajoute HAL converter si tu traites des HAL responses
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
        halConverter.setSupportedMediaTypes(List.of(HAL_JSON));
        halConverter.setObjectMapper(mapper);

        this.restTemplate.getMessageConverters().add(0, halConverter);
    }

    public <T> CompletableFuture<EntityModel<T>> getResource(URI url, ParameterizedTypeReference<EntityModel<T>> type) {
        return CompletableFuture.supplyAsync(() -> {
            RequestEntity<Void> request = RequestEntity.get(url).accept(HAL_JSON).build();
            LOG.debug("GET {}", url);
            return restTemplate.exchange(request, type).getBody();
        });
    }

    public <T, R> CompletableFuture<R> getDataList(URI url, ParameterizedTypeReference<R> type) {
        return CompletableFuture.supplyAsync(() -> {
            RequestEntity<Void> request = RequestEntity.get(url).accept(MediaType.APPLICATION_JSON).build();
            LOG.debug("GET {}", url);
            return restTemplate.exchange(request, type).getBody();
        });
    }

    public <T, B> CompletableFuture<T> postResource(URI uri, B body, ParameterizedTypeReference<T> returnType) {
        return CompletableFuture.supplyAsync(() -> {
            RequestEntity<B> request = RequestEntity
                    .post(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body);
            LOG.debug("POST to {}", uri);
            return restTemplate.exchange(request, returnType).getBody();
        });
    }
}
