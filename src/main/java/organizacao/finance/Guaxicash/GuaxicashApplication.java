package organizacao.finance.Guaxicash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


import java.sql.Connection;

@SpringBootApplication
@EnableScheduling
public class GuaxicashApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuaxicashApplication.class, args);
	}

}
