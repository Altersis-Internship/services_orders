package sockshop.orders.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import sockshop.orders.config.RestProxyTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Future;

import static org.springframework.hateoas.MediaTypes.HAL_JSON;

@Service
public class AsyncGetService {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private final RestProxyTemplate restProxyTemplate;

    private final RestTemplate halTemplate;

    @Autowired
    public AsyncGetService(RestProxyTemplate restProxyTemplate) {
        this.restProxyTemplate = restProxyTemplate;
        this.halTemplate = new RestTemplate(restProxyTemplate.getRestTemplate().getRequestFactory());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new Jackson2HalModule());
        MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
        halConverter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, MediaType.APPLICATION_JSON));
        halConverter.setObjectMapper(objectMapper);
        halTemplate.setMessageConverters(Collections.singletonList(halConverter));
    }

    @Async
    public <T> Future<EntityModel<T>> getResource(URI url, ParameterizedTypeReference<EntityModel<T>> type)
            throws InterruptedException, IOException {
        RequestEntity<Void> request = RequestEntity.get(url).accept(HAL_JSON).build();
        LOG.debug("Requesting: " + request.toString());
        EntityModel<T> body = halTemplate.exchange(request, type).getBody();
        LOG.debug("Received: " + body.toString());
        return new AsyncResult<>(body);
    }

    @Async
    public <T, R> Future<R> getDataList(URI url, ParameterizedTypeReference<R> type)
            throws InterruptedException, IOException {
        RequestEntity<Void> request = RequestEntity.get(url)
                .accept(type.getType().equals(CollectionModel.class) ? HAL_JSON : MediaType.APPLICATION_JSON)
                .build();
        LOG.debug("Requesting: " + request.toString());
        R body = halTemplate.exchange(request, type).getBody();
        LOG.debug("Received: " + body.toString());
        return new AsyncResult<>(body);
    }

    @Async
    public <T, B> Future<T> postResource(URI uri, B body, ParameterizedTypeReference<T> returnType)
            throws InterruptedException, IOException {
        RequestEntity<B> request = RequestEntity.post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(HAL_JSON, MediaType.APPLICATION_JSON)
                .body(body);
        LOG.debug("Requesting: " + request.toString());
        try {
            T responseBody = halTemplate.exchange(request, returnType).getBody();
            LOG.debug("Received: " + responseBody);
            return new AsyncResult<>(responseBody);
        } catch (HttpClientErrorException e) {
            LOG.error("HTTP error while posting to {}: {} - {}", uri, e.getStatusCode(), e.getResponseBodyAsString());
            throw new IOException("Failed to post resource to " + uri + ": " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        }
    }
}