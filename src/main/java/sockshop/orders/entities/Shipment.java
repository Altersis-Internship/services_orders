package sockshop.orders.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.UUID;

@Document(collection = "Shipment")
public class Shipment {

    @Id
    private String id;
    private String name;

    public Shipment() {
        this("");
    }

    public Shipment(String name) {
        this(UUID.randomUUID().toString(), name);
    }

    public Shipment(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // toString
    @Override
    public String toString() {
        return "Shipment{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    // equals et hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment shipment)) return false;
        return Objects.equals(id, shipment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}