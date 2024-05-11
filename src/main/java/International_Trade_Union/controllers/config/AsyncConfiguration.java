package International_Trade_Union.controllers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfiguration {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // Количество потоков в пуле по умолчанию
        executor.setMaxPoolSize(20); // Максимальное количество потоков в пуле
        executor.setQueueCapacity(200); // Размер очереди для задач, ожидающих выполнения
        return executor;
    }
}