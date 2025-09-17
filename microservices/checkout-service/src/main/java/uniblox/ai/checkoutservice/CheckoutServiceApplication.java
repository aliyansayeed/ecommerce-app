package uniblox.ai.checkoutservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "uniblox.ai.checkoutservice",
        "uniblox.ai.config","uniblox.ai.utils"   //  include base-config beans
})public class CheckoutServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckoutServiceApplication.class, args);
    }

}
