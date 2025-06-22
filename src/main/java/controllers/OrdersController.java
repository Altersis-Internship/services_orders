package controllers;

import entities.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import repositories.CustomerOrderRepository;
import resources.NewOrderResource;
import resources.PaymentRequest;
import resources.PaymentResponse;
import services.AsyncGetService;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RepositoryRestController
public class OrdersController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private AsyncGetService asyncGetService;

    @Autowired
    private config.OrdersConfigurationProperties config;

    @Value(value = "${http.timeout:5}")
    private long timeout;

    @Value("${simulate.latency:false}")
    private boolean simulateLatency;

    @Value("${simulate.cpu:false}")
    private boolean simulateCpu;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @WithSpan("fn newOrder")
    public @ResponseBody CustomerOrder newOrder(@SpanAttribute("item") @RequestBody NewOrderResource item) {
        Span span = Span.current();

        try {
            if (simulateLatency) {
                Thread.sleep(new Random().nextInt(2000) + 1000); // simulate 1-3 sec delay
                LOG.warn("Simulated latency injected");
            }

            if (simulateCpu) {
                for (int i = 0; i < 1_000_000; i++) {
                    Math.sqrt(i); // simulate heavy computation
                }
                LOG.warn("Simulated CPU spike executed");
            }

            if (item == null || item.customer == null || item.card == null || item.items == null) {
                throw new InvalidOrderException("Missing order data");
            }

            Future<EntityModel<Address>> addressFuture = asyncGetService.getResource(item.address,
                    new ParameterizedTypeReference<>() {});
            Address address = addressFuture.get(timeout, TimeUnit.SECONDS).getContent();

            Future<EntityModel<Customer>> customerFuture = asyncGetService.getResource(item.customer,
                    new ParameterizedTypeReference<>() {});
            Customer customer = customerFuture.get(timeout, TimeUnit.SECONDS).getContent();

            Future<EntityModel<Card>> cardFuture = asyncGetService.getResource(item.card,
                    new ParameterizedTypeReference<>() {});
            Card card = cardFuture.get(timeout, TimeUnit.SECONDS).getContent();

            Future<List<Item>> itemsFuture = asyncGetService.getDataList(item.items,
                    new ParameterizedTypeReference<>() {});
            List<Item> items = itemsFuture.get(timeout, TimeUnit.SECONDS);

            float amount = calculateTotal(items);

            PaymentRequest paymentRequest = new PaymentRequest(address, card, customer, amount);
            Future<PaymentResponse> paymentFuture = asyncGetService.postResource(config.getPaymentUri(),
                    paymentRequest, new ParameterizedTypeReference<>() {});
            PaymentResponse paymentResponse = paymentFuture.get(timeout, TimeUnit.SECONDS);

            if (paymentResponse == null || !paymentResponse.isAuthorised()) {
                throw new PaymentDeclinedException("Payment failed");
            }

            String customerId = parseId(customer.getId());
            Future<Shipment> shipmentFuture = asyncGetService.postResource(config.getShippingUri(),
                    new Shipment(customerId), new ParameterizedTypeReference<>() {});

            CustomerOrder order = new CustomerOrder(null, customerId, customer, address, card,
                    items, shipmentFuture.get(timeout, TimeUnit.SECONDS), Calendar.getInstance().getTime(), amount);

            return customerOrderRepository.save(order);

        } catch (TimeoutException e) {
            throw new IllegalStateException("Service timeout", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException("Error processing order", e);
        } finally {
            span.end();
        }
    }

    private String parseId(String href) {
        Matcher matcher = Pattern.compile("[\\w-]+$").matcher(href);
        if (!matcher.find()) throw new IllegalStateException("Invalid ID format: " + href);
        return matcher.group(0);
    }

    private float calculateTotal(List<Item> items) {
        float amount = 0F;
        float shipping = 4.99F;
        amount += items.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice()).sum();
        return amount + shipping;
    }

    @ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
    public static class PaymentDeclinedException extends IllegalStateException {
        public PaymentDeclinedException(String s) {
            super(s);
        }
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public static class InvalidOrderException extends IllegalStateException {
        public InvalidOrderException(String s) {
            super(s);
        }
    }

    @GetMapping("/orders")
    public String getOrders() {
        return "Hello from orders";
    }
 // Simulations de problèmes de performance
    private static final List<byte[]> memoryLeakList = new CopyOnWriteArrayList<>();

    @GetMapping("/simulate-latency")
    public String simulateLatencyEndpoint() throws InterruptedException {
        Thread.sleep(3000); // 3 secondes de pause
        LOG.warn("Latency simulated");
        return "Latence simulée";
    }

    @GetMapping("/simulate-cpu")
    public String simulateCpuEndpoint() {
        LOG.warn("CPU load simulation started");
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000) {
            Math.pow(Math.random(), Math.random()); // opération coûteuse CPU
        }
        return "Charge CPU simulée";
    }

    @GetMapping("/simulate-leak")
    public String simulateMemoryLeak() {
        LOG.warn("Memory leak simulation triggered");
        byte[] leak = new byte[10 * 1024 * 1024]; // 10 Mo
        memoryLeakList.add(leak);
        return "Fuite mémoire simulée : " + memoryLeakList.size() + " blocs";
    }

    @GetMapping("/simulate-thread")
    public String simulateThreadSpike() {
        LOG.warn("Thread spawn simulation");
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
            }
        }).start();
        return "Thread supplémentaire lancé";
    }

    @GetMapping("/simulate-error")
    public String simulateError() {
        LOG.error("Erreur simulée déclenchée");
        throw new RuntimeException("Erreur simulée");
    }
 // 5. Deadlock global
    @GetMapping("/chaos/deadlock")
    public String simulateDeadlockMayhem() {
        LOG.error("🔗 GLOBAL DEADLOCK INITIATED");
        Object lockA = new Object();
        Object lockB = new Object();
        
        new Thread(() -> {
            synchronized (lockA) {
                try { Thread.sleep(1000); } catch (Exception ignored) {}
                synchronized (lockB) {}
            }
        }).start();
        
        synchronized (lockB) {
            try { Thread.sleep(1000); } catch (Exception ignored) {}
            synchronized (lockA) {}
        }
        
        return "Deadlock global activé";
    }

}