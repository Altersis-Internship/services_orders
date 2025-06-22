package entities;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Item")
public class Item {

    @Id
    private String id;

    @NotNull(message = "Item Id must not be null")
    private String itemId;

    private int quantity;
    private float unitPrice;

    // Constructeurs
    public Item() {
        this(null, "", 1, 0F);
    }

    public Item(String id, String itemId, int quantity, float unitPrice) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Item(String itemId) {
        this(null, itemId, 1, 0F);
    }

    public Item(Item item, String id) {
        this(id, item.itemId, item.quantity, item.unitPrice);
    }

    public Item(Item item, int quantity) {
        this(item.id, item.itemId, quantity, item.unitPrice);
    }

    // Getters et setters standard
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(float unitPrice) {
        this.unitPrice = unitPrice;
    }

    // toString
    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", itemId='" + itemId + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }

    // equals basé sur itemId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;
        return itemId != null && itemId.equals(item.itemId);
    }

    // hashCode basé sur itemId
    @Override
    public int hashCode() {
        return itemId != null ? itemId.hashCode() : 0;
    }
}
