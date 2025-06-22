package entities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Card") // Représente une collection MongoDB nommée "Card"
public class Card {

    @Id // Clé primaire de l'objet (MongoDB _id)
    private String id;

    private String longNum;  // Numéro long de la carte
    private String expires;  // Date d'expiration
    private String ccv;      // Code de sécurité

    // Constructeur par défaut (nécessaire pour Spring Data et Jackson)
    public Card() {}

    // Constructeur avec tous les champs
    public Card(String id, String longNum, String expires, String ccv) {
        this.id = id;
        this.longNum = longNum;
        this.expires = expires;
        this.ccv = ccv;
    }

    // Constructeur sans id (utilisé lors de la création avant sauvegarde)
    public Card(String longNum, String expires, String ccv) {
        this(null, longNum, expires, ccv);
    }

    // Méthode toString utile pour le debug
    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", longNum='" + longNum + '\'' +
                ", expires='" + expires + '\'' +
                ", ccv='" + ccv + '\'' +
                '}';
    }

    // Deux objets Card sont égaux s’ils ont le même id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;              // même référence mémoire
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        return id != null && id.equals(card.getId()); // on évite NullPointerException
    }

    // Hash basé uniquement sur l’id
    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongNum() {
        return longNum;
    }

    public void setLongNum(String longNum) {
        this.longNum = longNum;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getCcv() {
        return ccv;
    }

    public void setCcv(String ccv) {
        this.ccv = ccv;
    }
}