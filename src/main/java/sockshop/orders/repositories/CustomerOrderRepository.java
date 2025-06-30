package sockshop.orders.repositories;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import sockshop.orders.entities.CustomerOrder;

import java.util.List;

@RepositoryRestResource(
        collectionResourceRel = "orders", // nom de la ressource dans le HAL JSON
        path = "orders",                  // chemin de l'URL REST exposée
        itemResourceRel = "order"        // nom de l'élément individuel dans le HAL JSON
)
public interface CustomerOrderRepository extends MongoRepository<CustomerOrder, String> {

    // http://localhost:8082/orders/search/customerId?custId=123
    @RestResource(path = "customerId", rel = "customerId")
    List<CustomerOrder> findByCustomerId(@Param("custId") String customerId);
}
