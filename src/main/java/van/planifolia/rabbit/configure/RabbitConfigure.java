package van.planifolia.rabbit.configure;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import van.planifolia.rabbit.enums.RabbitMQEnum;

/**
 * RabbitMQ 延迟消息处理流程：
 * <p>这是一个基于 RabbitMQ 的订单超时取消流程。通过设置 TTL 队列（延迟队列）和死信交换机（Dead Letter Exchange, DLX），
 * 实现消息延迟投递到消费者。如果订单未在指定的 TTL 时间内被处理，消息会被路由到死信交换机，进而到死信队列，由消费者进行超时处理。
 *
 * <h3>流程步骤：</h3>
 * <ol>
 *   <li>生产者将消息发送到延时队列交换机。</li>
 *   <li>延时队列交换机将消息路由到延迟队列（TTL队列）。</li>
 *   <li>消息在延迟队列中等待，直到TTL时间到期。</li>
 *   <li>当消息在延迟队列中过期且未被消费时，消息会成为“死信”，并被转发到死信交换机。</li>
 *   <li>死信交换机将消息路由到死信队列。</li>
 *   <li>绑定了死信队列的消费者从队列中获取消息并进行处理，如订单取消。</li>
 * </ol>
 *
 * <h3>流程图：</h3>
 * <pre>
 * +--------------------------+
 * | 延迟队列生产者           |
 * +----------+---------------+
 *            |
 *            v
 * +----------+---------------+
 * | 延时队列交换机（TTL交换机） |
 * +----------+---------------+
 *            |
 *            v
 * +----------+---------------+
 * | 延迟队列（TTL队列）        |
 * | 设置了消息存活时间         |
 * +----------+---------------+
 *            |
 *   (消息延迟时间过期，未被消费)
 *            |
 *            v
 * +----------+---------------+
 * | 死信交换机                |
 * +----------+---------------+
 *            |
 *            v
 * +----------+---------------+
 * | 死信队列                  |
 * +----------+---------------+
 *            |
 *            v
 * +----------+---------------+
 * | 绑定了死信队列的消费者   |
 * | 执行相应的超时处理逻辑   |
 * +--------------------------+
 * </pre>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>TTL 队列必须正确配置 `x-dead-letter-exchange` 和 `x-dead-letter-routing-key`。</li>
 *   <li>死信交换机和死信队列的配置必须确保消息能够正确地从 TTL 队列过期后路由到死信队列。</li>
 * </ul>
 *
 * @Author: Planifolia.Van
 * @Date: 2024/8/29 下午4:24
 */

public class RabbitConfigure {

    /**
     * 配置订单交换机
     *
     * @return 订单交换机
     */
    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder
                // 设置交换机为直连交换机
                .directExchange(RabbitMQEnum.ORDER_QUEUE.getExchange())
                // 开启持久化
                .durable(true)
                .build();
    }

    /**
     * 注册普通订单队列
     *
     * @return 普通订单队列
     */
    @Bean
    public Queue orderQueue() {
        // 注册队列并且设置持久化
        return new Queue(RabbitMQEnum.ORDER_QUEUE.getQueueName(), true);
    }

    /**
     * 队列交换机绑定器
     *
     * @param orderExchange 订单交换机
     * @param orderQueue    订单队列
     * @return 绑定结果
     */
    @Bean
    public Binding orderQueueExchangeBinder(DirectExchange orderExchange, Queue orderQueue) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(RabbitMQEnum.ORDER_QUEUE.getRouteKey());
    }

    /**
     * 创建TTL延迟交换机
     *
     * @return 延迟交换机Bean
     */
    @Bean
    public DirectExchange orderTTLExchange() {
        return ExchangeBuilder
                .directExchange(RabbitMQEnum.ORDER_TTL_QUEUE.getExchange())
                .durable(true)
                .build();
    }

    /**
     * 创建TTL延迟队列
     *
     * @return 延时队列Bean
     */
    @Bean
    public Queue orderTTLQueue() {
        return QueueBuilder
                .durable(RabbitMQEnum.ORDER_TTL_QUEUE.getQueueName())
                //延时队列需要绑定死信队列以便于消息过期后立马进入死信交换机
                .withArgument("x-dead-letter-exchange", RabbitMQEnum.ORDER_DEATH_QUEUE.getExchange())
                //创建延迟队列的时候绑定路由键
                .withArgument("x-dead-letter-routing-key", RabbitMQEnum.ORDER_DEATH_QUEUE.getRouteKey())
                .build();
    }

    /**
     * 延时队列与延时交换机绑定
     *
     * @param orderTTLExchange 死信交换机
     * @param orderTTLQueue    死信队列
     * @return 绑定结果
     */
    @Bean
    public Binding orderTTLQueueExchangeBinder(DirectExchange orderTTLExchange, Queue orderTTLQueue) {
        return BindingBuilder.bind(orderTTLQueue).to(orderTTLExchange).with(RabbitMQEnum.ORDER_TTL_QUEUE.getRouteKey());
    }

    /**
     * 创建死信交换机
     *
     * @return 死信交换机
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(RabbitMQEnum.ORDER_DEATH_QUEUE.getExchange()).durable(true).build();
    }

    /**
     * 创建死信队列
     *
     * @return 死信队列
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(RabbitMQEnum.ORDER_DEATH_QUEUE.getQueueName(), true);
    }

    /**
     * 绑定死信队列与死信交换机
     *
     * @param deadLetterExchange 死信交换新
     * @param deadLetterQueue    死信队列
     * @return 绑定结果
     */
    @Bean
    public Binding deadLetterBinding(DirectExchange deadLetterExchange, Queue deadLetterQueue) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(RabbitMQEnum.ORDER_DEATH_QUEUE.getRouteKey());
    }


}
