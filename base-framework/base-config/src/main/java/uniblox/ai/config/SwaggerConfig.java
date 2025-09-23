package uniblox.ai.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Microservices API")
                        .description("API documentation for Admin, Cart, Checkout, Discount, and Order services")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("MOHAMMED ZUBEDI")
                                .email("ALIYANSAYEED@GMAIL.COM")
                        )
                );
    }
}
