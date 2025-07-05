package sockshop.orders.entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Document(collection = "Customer")
public class Customer {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String username;

    @DBRef(lazy = true)
    private List<Address> addresses = new ArrayList<>();

    @DBRef(lazy = true)
    private List<Card> cards = new ArrayList<>();

    // Constructeurs
    public Customer() {
    }

    public Customer(String id, String firstName, String lastName, String username, List<Address> addresses, List<Card> cards) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.addresses = addresses;
        this.cards = cards;
    }

    public Customer(String firstName, String lastName, String username, List<Address> addresses, List<Card> cards) {
        this(null, firstName, lastName, username, addresses, cards);
    }

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // toString
    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", addresses=" + addresses +
                ", cards=" + cards +
                '}';
    }

    // equals et hashCode basés sur id et username
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer customer)) return false;
        return Objects.equals(id, customer.id) &&
               Objects.equals(username, customer.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
