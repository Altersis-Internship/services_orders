package sockshop.orders.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Objects;

@Document(collection = "Address")
public class Address {

    @Id
    private String id;

    private String number;
    private String street;
    private String city;
    private String postcode;
    private String country;

    // Constructeurs
    public Address() {}

    public Address(String number, String street, String city, String postcode, String country) {
        this(null, number, street, city, postcode, country);
    }

    public Address(String id, String number, String street, String city, String postcode, String country) {
        this.id = id;
        this.number = number;
        this.street = street;
        this.city = city;
        this.postcode = postcode;
        this.country = country;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // equals & hashCode avec Objects
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;
        Address address = (Address) o;
        return Objects.equals(id, address.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString
    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
