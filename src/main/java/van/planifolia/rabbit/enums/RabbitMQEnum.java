package van.planifolia.rabbit.enums;


import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
public enum RabbitMQEnum {


    /**
     * 订单队列
     */
    ORDER_QUEUE("van.order.exchange", "van.order.queue", "order"),
    /**
     * 订单超时队列
     */
    ORDER_TTL_QUEUE("van.order.ttl.exchange", "van.order.ttl.queue", "order.ttl"),

    /**
     * 订单死信队列
     */
    ORDER_DEATH_QUEUE("van.order.dead.exchange", "van.order.dead.queue", "van.order.dead");

    /**
     * 交换机名称
     */
    public final String exchange;
    /**
     * 队列名称
     */
    public final String queueName;
    /**
     * 路由Key
     */
    public final String routeKey;

    RabbitMQEnum(String exchange, String queueName, String routeKey) {
        this.exchange = exchange;
        this.queueName = queueName;
        this.routeKey = routeKey;
    }
}
