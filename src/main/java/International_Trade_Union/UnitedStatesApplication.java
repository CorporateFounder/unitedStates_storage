package International_Trade_Union;

import International_Trade_Union.utils.UtilsCreatedDirectory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.io.IOException;


@SpringBootApplication
@EnableAspectJAutoProxy
public class UnitedStatesApplication {
	public static void main(String[] args) throws IOException {
		SpringApplication.run(UnitedStatesApplication.class, args);
	}

}
