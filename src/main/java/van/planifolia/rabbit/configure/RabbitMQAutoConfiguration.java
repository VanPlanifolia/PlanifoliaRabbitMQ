package van.planifolia.rabbit.configure;


import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import van.planifolia.rabbit.product.RabbitMessageSender;


@Configuration
@EnableConfigurationProperties(RabbitProperties.class)
public class RabbitMQAutoConfiguration {

    /**
     * 注册rabbitConfigure
     *
     * @return 注册的rabbitMQ配置
     */
    @Bean
    public RabbitConfigure rabbitConfigure() {
        return new RabbitConfigure();
    }

    /**
     * 注册rabbitMessageSender
     *
     * @return 注册的rabbitMessageSender
     */
    @Bean
    public RabbitMessageSender rabbitMessageSender() {
        return new RabbitMessageSender();
    }

}

