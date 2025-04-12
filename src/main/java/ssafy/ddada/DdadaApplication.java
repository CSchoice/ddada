package ssafy.ddada;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableScheduling
@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"ssafy.ddada"})
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class DdadaApplication {

    public static void main(String[] args) {
        System.out.println("=== ENV CHECK START ===");
        System.out.println("DATASOURCE_URL: " + System.getenv("DATASOURCE_URL"));
        System.out.println("DATASOURCE_USERNAME: " + System.getenv("DATASOURCE_USERNAME"));
        System.out.println("DATASOURCE_PASSWORD: " + System.getenv("DATASOURCE_PASSWORD"));
        System.out.println("=== ENV CHECK END ===");

        SpringApplication.run(DdadaApplication.class, args);
    }

}
