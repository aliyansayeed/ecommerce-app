package uniblox.ai.discountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "uniblox.ai.discountservice",
        "uniblox.ai.config","uniblox.ai.utils"   //  include base-config beans
})
public class DiscountserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscountserviceApplication.class, args);
    }
}
