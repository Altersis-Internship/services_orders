package sockshop.orders.entities;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("Cart") // Spécifie que cette classe est mappée à la collection MongoDB "Cart"
public class Cart {

    @NotNull // Indique que le champ customerId ne doit pas être null (validation au niveau Java)
    private String customerId;

    @Id // Identifiant unique MongoDB (équivalent au champ _id)
    private String id;

    @DBRef // Référence vers d'autres documents MongoDB (ici des objets de type Item)
    private List<Item> items = new ArrayList<>();

    // Constructeur avec customerId
    public Cart(String customerId) {
        this.customerId = customerId;
    }

    // Constructeur par défaut
    public Cart() {
        this(null);
    }

    // Retourne la liste des items (contenu du panier)
    public List<Item> contents() {
        return items;
    }

    // Ajoute un item au panier
    public Cart add(Item item) {
        items.add(item);
        return this;
    }

    // Supprime un item du panier
    public Cart remove(Item item) {
        items.remove(item);
        return this;
    }

    // Getters et Setters
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    // toString utile pour affichage ou logs
    @Override
    public String toString() {
        return "Cart{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", items=" + items +
                '}';
    }
}

