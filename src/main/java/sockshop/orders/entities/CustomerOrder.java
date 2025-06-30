package sockshop.orders.entities;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "CustomerOrder")
public class CustomerOrder {

    @Id
    private String id;

    private String customerId;

    private Customer customer;

    private Address address;

    private Card card;

    private Collection<Item> items;

    private Shipment shipment;

    private Date date = Calendar.getInstance().getTime();

    private float total;

    public CustomerOrder() {
    }

    public CustomerOrder(String id, String customerId, Customer customer, Address address, Card card,
                         Collection<Item> items, Shipment shipment, Date date, float total) {
        this.id = id;
        this.customerId = customerId;
        this.customer = customer;
        this.address = address;
        this.card = card;
        this.items = items;
        this.shipment = shipment;
        this.date = date;
        this.total = total;
    }

    // Getters et setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Collection<Item> getItems() {
        return items;
    }

    // Pour éviter conflit Jackson, on ne garde qu'un seul setter
    public void setItems(Collection<Item> items) {
        this.items = items;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public void setShipment(Shipment shipment) {
        this.shipment = shipment;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    // toString
    @Override
    public String toString() {
        return "CustomerOrder{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customer=" + customer +
                ", address=" + address +
                ", card=" + card +
                ", items=" + items +
                ", shipment=" + shipment +
                ", date=" + date +
                ", total=" + total +
                '}';
    }

    // equals & hashCode (basés sur id)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerOrder that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

