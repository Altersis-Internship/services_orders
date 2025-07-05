package sockshop.orders.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "HealthCheck")
public class HealthCheck {

    @Id
    private String id;

    private String service;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date date = Calendar.getInstance().getTime();

    public HealthCheck() {
    }

    public HealthCheck(String service, String status, Date date) {
        this.service = service;
        this.status = status;
        this.date = date;
    }


    // Getters & Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
       this.id = id;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    // toString

    @Override
    public String toString() {
        return "HealthCheck{" +
                "id='" + id + '\'' +
                ", service='" + service + '\'' +
                ", status='" + status + '\'' +
                ", date=" + date +
                '}';
    }

    // equals & hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthCheck that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

