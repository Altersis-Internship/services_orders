package sockshop.orders.resources;

import org.hibernate.validator.constraints.URL;
import java.net.URI;

public class NewOrderResource {
    @URL
    public URI customer;

    @URL
    public URI address;

    @URL
    public URI card;

    @URL
    public URI items;

    // Champ optionnel pour simuler les problèmes
    public String simulate;

    // Optional getters/setters (utile si tu utilises un framework comme Jackson ou MapStruct)
    public URI getCustomer() {
        return customer;
    }

    public void setCustomer(URI customer) {
        this.customer = customer;
    }

    public URI getAddress() {
        return address;
    }

    public void setAddress(URI address) {
        this.address = address;
    }

    public URI getCard() {
        return card;
    }

    public void setCard(URI card) {
        this.card = card;
    }

    public URI getItems() {
        return items;
    }

    public void setItems(URI items) {
        this.items = items;
    }

    public String getSimulate() {
        return simulate;
    }

    public void setSimulate(String simulate) {
        this.simulate = simulate;
    }
}
