package van.planifolia.rabbit.product;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Configuration;
import van.planifolia.rabbit.enums.RabbitMQEnum;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author: Planifolia.Van
 * @Date: 2024/9/13 上午10:46
 */
@Slf4j
@Configuration
public class RabbitMessageSender {
    @Resource
    private AmqpTemplate amqpTemplate;

    /**
     * 通过默认的队列交换机发送普通消息
     *
     * @param message 消息体
     * @param <T>     泛型
     */
    public <T> void sendMessage(T message) {
        String jsonStr = JSONObject.toJSONString(message);
        amqpTemplate.convertAndSend(RabbitMQEnum.ORDER_QUEUE.getExchange(), RabbitMQEnum.ORDER_QUEUE.getRouteKey(), jsonStr, msg -> {
            log.info("rabbitMQ发送消息成功！交换机:{},路由器：{}", RabbitMQEnum.ORDER_QUEUE.getExchange(), RabbitMQEnum.ORDER_QUEUE.getRouteKey());
            return msg;
        });
    }

    /**
     * 通过默认的队列交换机发送延时消息
     *
     * @param message   消息体
     * @param delayTime 推迟时间
     * @param <T>       泛型
     */
    public <T> void sendTTLMessage(T message, Integer delayTime) {
        String jsonStr = JSONObject.toJSONString(message);
        amqpTemplate.convertAndSend(RabbitMQEnum.ORDER_TTL_QUEUE.getExchange(), RabbitMQEnum.ORDER_TTL_QUEUE.getRouteKey(), jsonStr, msg -> {
            log.info("rabbitMQ发送延时消息成功！延迟时间:{}s,死信队列:{},交换机:{},路由器：{}", delayTime, RabbitMQEnum.ORDER_DEATH_QUEUE.getQueueName(), RabbitMQEnum.ORDER_DEATH_QUEUE.getExchange(), RabbitMQEnum.ORDER_DEATH_QUEUE.getRouteKey());
            msg.getMessageProperties().setExpiration(String.valueOf(delayTime * 1000));
            return msg;
        });
    }

    /**
     * 通过默认的队列交换机发送普通消息
     *
     * @param message 消息体
     * @param <T>     泛型
     */
    public <T> void sendMessage(T message, String yourExchange, String yourRouteKey) {
        String jsonStr = JSONObject.toJSONString(message);
        amqpTemplate.convertAndSend(yourExchange, yourRouteKey, jsonStr, msg -> {
            log.info("rabbitMQ自定义发送消息成功！交换机:{},路由器：{}", yourExchange, yourRouteKey);
            return msg;
        });
    }

    /**
     * 通过默认的队列交换机发送普通消息
     *
     * @param message   消息体
     * @param delayTime 推迟时间
     * @param <T>       泛型
     */
    public <T> void sendTTLMessage(T message, String yourExchange, String yourRouteKey, Integer delayTime) {
        String jsonStr = JSONObject.toJSONString(message);
        amqpTemplate.convertAndSend(yourExchange, yourRouteKey, jsonStr, msg -> {
            log.info("rabbitMQ自定义发送延时消息成功！延迟时间:{}s,交换机:{},路由器：{}", delayTime, yourExchange, yourRouteKey);
            msg.getMessageProperties().setExpiration(String.valueOf(delayTime * 1000));
            return msg;
        });
    }
}
