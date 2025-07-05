package sockshop.orders.test;


import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")  // Activé uniquement si spring.profiles.active=dev
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/sleep")
    public String simulateSleep() throws InterruptedException {
        Thread.sleep(5000);  // Simule une latence de 5 secondes
        return "Réponse lente simulée (sleep)";
    }

    @GetMapping("/error")
    public String simulateError() {
        throw new RuntimeException("Erreur simulée !");
    }

    @GetMapping("/cpu")
    public String simulateCpu() {
        long end = System.currentTimeMillis() + 5000;  // 5 secondes
        while (System.currentTimeMillis() < end) {
            Math.sqrt(Math.random());  // Charge CPU
        }
        return "Charge CPU simulée";
    }
}
