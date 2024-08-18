package International_Trade_Union.controllers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, new ConditionalBigDecimalSerializer());
        objectMapper.registerModule(module);

        return objectMapper;
    }
}
