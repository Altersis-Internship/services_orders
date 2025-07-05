package sockshop.orders.controllers;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import sockshop.orders.entities.*;
import sockshop.orders.repositories.CustomerOrderRepository;
import sockshop.orders.resources.NewOrderResource;
import sockshop.orders.resources.PaymentRequest;
import sockshop.orders.resources.PaymentResponse;
import sockshop.orders.services.AsyncGetService;

import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping
public class OrdersController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private AsyncGetService asyncGetService;

    @Autowired
    private sockshop.orders.config.OrdersConfigurationProperties config;

    @Value(value = "${http.timeout:5}")
    private long timeout;

    // Simulation config flags (set in application.properties)
    @Value("${simulate.latency:false}")
    private boolean simulateLatency;

    @Value("${simulate.cpu:false}")
    private boolean simulateCpu;

    @Value("${simulate.leak:false}")
    private boolean simulateLeak;

    @Value("${simulate.thread:false}")
    private boolean simulateThread;

    @Value("${simulate.deadlock:false}")
    private boolean simulateDeadlock;

    @Value("${simulate.error:false}")
    private boolean simulateError;

    @Value("${orders.payment-uri}")
    private String paymentUri;

    // Static counter for successful orders
    private static int successfulOrderCount = 0;

    private static final List<byte[]> memoryLeakList = new CopyOnWriteArrayList<>();

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path = "/orders", consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    @WithSpan("fn newOrder")
    public @ResponseBody CustomerOrder newOrder(@SpanAttribute("item") @RequestBody NewOrderResource item) {
        Span span = Span.current();

        try {
            simulateProblemsIfEnabled();

            if (item.address == null || item.customer == null || item.card == null || item.items == null) {
                throw new InvalidOrderException("Invalid order request. Order requires customer, address, card and items.");
            }

            span.addEvent("Order request received");

            LOG.debug("Starting calls");
            Future<EntityModel<Address>> addressFuture = asyncGetService.getResource(item.address,
                    new ParameterizedTypeReference<EntityModel<Address>>() {});
            Address address = addressFuture.get(timeout, TimeUnit.SECONDS).getContent();
            span.setAttribute("address", address.toString());
            Future<EntityModel<Customer>> customerFuture = asyncGetService.getResource(item.customer,
                    new ParameterizedTypeReference<EntityModel<Customer>>() {});
            Customer customer = customerFuture.get(timeout, TimeUnit.SECONDS).getContent();
            span.setAttribute("customer", customer.toString()); // Corrected to customer
            Future<EntityModel<Card>> cardFuture = asyncGetService.getResource(item.card,
                    new ParameterizedTypeReference<EntityModel<Card>>() {});
            Card card = cardFuture.get(timeout, TimeUnit.SECONDS).getContent();
            span.setAttribute("card", card.toString());
            Future<List<Item>> itemsFuture = asyncGetService.getDataList(item.items,
                    new ParameterizedTypeReference<List<Item>>() {});
            List<Item> items = itemsFuture.get(timeout, TimeUnit.SECONDS);
            span.setAttribute("items", items.toString());
            LOG.debug("End of calls.");

            float amount = calculateTotal(itemsFuture.get(timeout, TimeUnit.SECONDS));
            span.setAttribute("amount", amount);

            // Call payment service to make sure they've paid
            PaymentRequest paymentRequest = new PaymentRequest(
                    addressFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    cardFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    customerFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    amount);
            LOG.info("Sending payment request: " + paymentRequest);
            Future<PaymentResponse> paymentFuture = asyncGetService.postResource(
                    URI.create(paymentUri),
                    paymentRequest,
                    new ParameterizedTypeReference<PaymentResponse>() {});
            PaymentResponse paymentResponse = paymentFuture.get(timeout, TimeUnit.SECONDS);
            LOG.info("Received payment response: " + paymentResponse);
            span.setAttribute("paymentResponse", paymentResponse.toString());
            if (!paymentResponse.isAuthorised()) {
                span.setAttribute("paymentStatus", "unauthorised");
                throw new PaymentDeclinedException(paymentResponse.getMessage());
            }
            span.setAttribute("paymentStatus", "authorised");

            // Ship
            String customerId = parseId(customerFuture.get(timeout, TimeUnit.SECONDS).getContent().getId());
            Future<Shipment> shipmentFuture = asyncGetService.postResource(config.getShippingUri(), new Shipment(customerId),
                    new ParameterizedTypeReference<Shipment>() {});

            CustomerOrder order = new CustomerOrder(
                    null,
                    customerId,
                    customerFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    addressFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    cardFuture.get(timeout, TimeUnit.SECONDS).getContent(),
                    itemsFuture.get(timeout, TimeUnit.SECONDS),
                    shipmentFuture.get(timeout, TimeUnit.SECONDS),
                    Calendar.getInstance().getTime(),
                    amount);
            LOG.debug("Received data: " + order.toString());
            span.setAttribute("order", order.toString());

            CustomerOrder savedOrder = customerOrderRepository.save(order);
            LOG.debug("Saved order: " + savedOrder);
            span.setAttribute("saved order", savedOrder.toString());

            // Increment counter on successful save
            successfulOrderCount++;
            LOG.info("Successful order count: {}", successfulOrderCount);

            return savedOrder;
        } catch (TimeoutException e) {
            throw new IllegalStateException("Unable to create order due to timeout from one of the services.", e);
        } catch (InterruptedException | IOException | ExecutionException e) {
            throw new IllegalStateException("Unable to create order due to unspecified IO error.", e);
        } finally {
            span.end();
        }
    }

    @GetMapping(path = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @WithSpan("fn getAllOrders")
    public List<CustomerOrder> getAllOrders() throws InterruptedException {
        simulateProblemsIfEnabled();
        return customerOrderRepository.findAll();
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

    private void simulateProblemsIfEnabled() throws InterruptedException {
        if (simulateLatency) {
            Thread.sleep(3000);
            LOG.warn("🕒 Simulated latency (3s)");
        }

        if (simulateCpu) {
            for (int i = 0; i < 5_000_000; i++) {
                Math.log(Math.sqrt(i + 1));
            }
            LOG.warn("🔥 Simulated CPU spike");
        }

        if (simulateLeak) {
            byte[] leak = new byte[10 * 1024 * 1024];
            memoryLeakList.add(leak);
            LOG.warn("💾 Simulated memory leak ({} blocks)", memoryLeakList.size());
        }

        if (simulateThread) {
            new Thread(() -> {
                while (true) {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                }
            }).start();
            LOG.warn("🧵 Simulated thread creation");
        }

        if (simulateDeadlock) {
            final Object lock1 = new Object();
            final Object lock2 = new Object();

            Thread t1 = new Thread(() -> {
                synchronized (lock1) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                    synchronized (lock2) {
                        LOG.warn("🔒 Thread 1 acquired both locks");
                    }
                }
            });

            Thread t2 = new Thread(() -> {
                synchronized (lock2) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {}
                    synchronized (lock1) {
                        LOG.warn("🔒 Thread 2 acquired both locks");
                    }
                }
            });

            t1.start();
            t2.start();

            LOG.warn("🔒 Simulated deadlock launched with 2 threads");
        }

        // New logic: Fail the 6th request when simulate.error is true
        if (simulateError && successfulOrderCount >=5) {
            LOG.error("💥 Simulated error thrown (6th request or more)");
            throw new RuntimeException("Simulated error: 6th request failed");
        }
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

    @GetMapping("/test-mock")
    public String testMockServer() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject("http://users-orders-mock:1080/addresses/57a98d98e4b00679b4a830ad", String.class);
            return response;
        } catch (Exception e) {
            LOG.error("MockServer unreachable: {}", e.getMessage());
            return "MockServer ERROR: " + e.getMessage();
        }
    }
}