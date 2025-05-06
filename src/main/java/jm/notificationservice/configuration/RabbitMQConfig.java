package jm.notificationservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "notificationExchange";
    public static final String PUSH_ROUTING_KEY = "notification.push";
    public static final String EMAIL_ROUTING_KEY = "notification.email";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setUri("amqp://guest:guest@localhost:5672");
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue pushQueue() {
        return QueueBuilder.durable("pushQueue")
                       .withArgument("x-max-priority", 100)
                       .build();
    }

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable("emailQueue")
                       .withArgument("x-max-priority", 100)
                       .build();
    }

    @Bean
    public Binding pushBinding() {
        return BindingBuilder.bind(pushQueue())
                       .to(exchange())
                       .with(PUSH_ROUTING_KEY);
    }

    @Bean
    public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue())
                       .to(exchange())
                       .with(EMAIL_ROUTING_KEY);
    }
}