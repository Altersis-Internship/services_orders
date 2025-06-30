package sockshop.orders.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.net.URI;

@ConfigurationProperties(prefix = "orders")
public class OrdersConfigurationProperties {
    private URI paymentUri;
    private URI shippingUri;

    public URI getPaymentUri() {
        return paymentUri;
    }

    public void setPaymentUri(URI paymentUri) {
        this.paymentUri = paymentUri;
    }

    public URI getShippingUri() {
        return shippingUri;
    }

    public void setShippingUri(URI shippingUri) {
        this.shippingUri = shippingUri;
    }
}