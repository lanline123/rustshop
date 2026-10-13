// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.retry.support.RetryTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // 交换机名称
    // 直连交换机（普通交换机）
    public static final String DIRECT_EXCHANGE = "direct_exchange";
    // 延迟交换机
    public static final String DELAY_EXCHANGE = "delay_exchange";

    // 队列名称
    public static final String SEND_SMS_QUEUE = "sendSmsQueue";
    public static final String ORDER_CANCEL_QUEUE = "orderCancelQueue";
    public static final String AUTO_MESSAGE_QUEUE = "autoMessageQueue";
    public static final String ORDER_CONFIRM_RECEIPT_QUEUE = "orderConfirmReceiptQueue";
    public static final String AUTO_RETURN_GOODS_QUEUE = "autoReturnGoodsQueue";
    public static final String AFTERSALES_AUTO_RECEIPT_QUEUE = "aftersalesAutoReceiptQueue";
    public static final String STORE_SETTLEMENT_QUEUE = "storeSettlementQueue";
    public static final String TRANSLATION_NAME_QUEUE = "translationNameQueue";
    public static final String SALESMAN_SETTLEMENT_QUEUE = "salesmanSettlementQueue";
    public static final String SALESMAN_UPDATE_QUEUE = "salesmanUpdateQueue";
    public static final String VIP_UPDATE_QUEUE = "vipUpdateQueue";
    public static final String VENDOR_DISCONTINUE_QUEUE = "vendorDiscontinueQueue";
    public static final String VENDOR_SETTLEMENT_QUEUE = "vendorSettlementQueue";
    public static final String PRINT_ORDER_QUEUE = "printOrderQueue";
    // 生成对账单
    public static final String GENERATE_STATEMENT_QUEUE = "generateStatementQueue";
    public static final String REFUND_SETTLEMENT_QUEUE = "refundSettlementQueue";
    public static final String GROUPON_RECORD_QUEUE = "grouponRecordQueue";
    /** 订单完成后延迟赠送积分队列 */
    public static final String ORDER_COMPLETE_SEND_POINT_QUEUE = "orderCompleteSendPointQueue";
    /** 自提订单预售自动备货延时队列 */
    public static final String PICKUP_AUTO_STOCK_QUEUE = "pickupAutoStockQueue";
    public static final String BATCH_SYNC = "batchSyncQueue";
    public static final String SINGLE_SYNC = "singleSyncQueue";
    public static final String PRODUCT_ES_SYNC_FAILED_QUEUE = "productEsSyncFailedQueue";
    public static final String PRODUCT_ES_SYNC_FAILED_EXCHANGE = "product.es.sync.failed.exchange";

    // Routing Key
    public static final String SEND_SMS_ROUTING_KEY = "send.sms.routing.key";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel.routing.key";
    public static final String AUTO_MESSAGE_ROUTING_KEY = "auto.message.routing.key";
    public static final String ORDER_CONFIRM_RECEIPT_ROUTING_KEY = "order.confirm.receipt.routing.key";
    public static final String AUTO_RETURN_GOODS_QUEUE_ROUTING_KEY = "auto.return.goods.routing.key";
    public static final String AFTERSALES_AUTO_RECEIPT_ROUTING_KEY = "aftersales.auto.receipt.routing.key";
    public static final String STORE_SETTLEMENT_ROUTING_KEY = "store.settlement.routing.key";
    public static final String TRANSLATION_NAME_ROUTING_KEY = "translation.name.routing.key";
    public static final String SALESMAN_SETTLEMENT_ROUTING_KEY = "salesman.settlement.routing.key";
    public static final String SALESMAN_UPDATE_ROUTING_KEY = "salesman.update.routing.key";
    public static final String VIP_UPDATE_ROUTING_KEY = "vip.update.routing.key";
    public static final String VENDOR_DISCONTINUE_ROUTING_KEY = "vendor.discontinue.routing.key";
    public static final String VENDOR_SETTLEMENT_ROUTING_KEY = "vendor.settlement.routing.key";
    public static final String PRINT_ORDER_ROUTING_KEY = "print.order.routing.key";
    public static final String GENERATE_STATEMENT_ROUTING_KEY = "generate.statement.routing.key";
    public static final String REFUND_SETTLEMENT_ROUTING_KEY = "refund.settlement.routing.key";
    public static final String GROUPON_RECORD_ROUTING_KEY = "groupon.record.routing.key";
    /** 订单完成后延迟赠送积分 */
    public static final String ORDER_COMPLETE_SEND_POINT_ROUTING_KEY = "order.complete.send.point.routing.key";
    /** 自提订单预售自动备货延时触发 */
    public static final String PICKUP_AUTO_STOCK_ROUTING_KEY = "pickup.auto.stock.routing.key";
    public static final String BATCH_SYNC_ROUTING_KEY = "batch.sync.routing.key";
    public static final String SINGLE_SYNC_ROUTING_KEY = "single.sync.routing.key";
    public static final String PRODUCT_ES_SYNC_FAILED_ROUTING_KEY = "product.es.sync.failed.routing.key";

    /**
     * 直连交换机（普通交换机）
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(DIRECT_EXCHANGE);
    }

    /**
     * 延迟交换机
     */
    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAY_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Queue grouponRecordQueue() {
        return QueueBuilder.durable(GROUPON_RECORD_QUEUE).build();
    }

    @Bean
    public Binding grouponRecordBinding() {
        return BindingBuilder.bind(grouponRecordQueue()).to(delayExchange()).with(GROUPON_RECORD_ROUTING_KEY).noargs();
    }

    /**
     * 短信队列
     */
    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SEND_SMS_QUEUE).build();
    }

    /**
     * 订单取消延迟队列（消息延迟机制）
     */
    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    /**
     * 自动回复延迟队列（消息延迟机制）
     */
    @Bean
    public Queue autoReplyQueue() {
        return QueueBuilder.durable(AUTO_MESSAGE_QUEUE).build();
    }

    @Bean
    public Queue orderConfirmReceiptQueue() {
        return QueueBuilder.durable(ORDER_CONFIRM_RECEIPT_QUEUE).build();
    }

    @Bean
    public Queue storeSettlementQueue() {
        return QueueBuilder.durable(STORE_SETTLEMENT_QUEUE).build();
    }

    @Bean
    public Queue autoReturnGoodsQueue() {
        return QueueBuilder.durable(AUTO_RETURN_GOODS_QUEUE).build();
    }

    @Bean
    public Queue aftersalesAutoReceiptQueue() {
        return QueueBuilder.durable(AFTERSALES_AUTO_RECEIPT_QUEUE).build();
    }

    /**
     * 翻译队列
     */
    @Bean
    public Queue translationNameQueue() {
        return QueueBuilder.durable(TRANSLATION_NAME_QUEUE).build();
    }

    @Bean
    public Queue salesmanSettlementQueue() {
        return QueueBuilder.durable(SALESMAN_SETTLEMENT_QUEUE).build();
    }

    @Bean
    public Queue salesmanUpdateQueue() {
        return QueueBuilder.durable(SALESMAN_UPDATE_QUEUE).build();
    }

    @Bean
    public Queue vipUpdateQueue() {
        return QueueBuilder.durable(VIP_UPDATE_QUEUE).build();
    }

    /**
     * 打印订单队列
     */
    @Bean
    public Queue printOrderQueue() {
        return QueueBuilder.durable(PRINT_ORDER_QUEUE).build();
    }

    @Bean
    public Queue vendorDiscontinueQueue() {
        return QueueBuilder.durable(VENDOR_DISCONTINUE_QUEUE).build();
    }

    @Bean
    public Queue vendorSettlementQueue() {
        return QueueBuilder.durable(VENDOR_SETTLEMENT_QUEUE).build();
    }

    // 生成对账单
    @Bean
    public Queue generateStatementQueue() {
        return QueueBuilder.durable(GENERATE_STATEMENT_QUEUE).build();
    }

    @Bean
    public Queue refundSettlementQueue() {
        return QueueBuilder.durable(REFUND_SETTLEMENT_QUEUE).build();
    }

    /**
     * 订单完成后延迟赠送积分队列（延迟交换机）
     */
    @Bean
    public Queue orderCompleteSendPointQueue() {
        return QueueBuilder.durable(ORDER_COMPLETE_SEND_POINT_QUEUE).build();
    }

    /**
     * 自提订单预售自动备货延时队列（延迟交换机）
     */
    @Bean
    public Queue pickupAutoStockQueue() {
        return QueueBuilder.durable(PICKUP_AUTO_STOCK_QUEUE).build();
    }

    @Bean
    public Queue batchSyncQueue() {
        return QueueBuilder.durable(BATCH_SYNC).build();
    }

    @Bean
    public Queue singleSyncQueue() {
        return QueueBuilder.durable(SINGLE_SYNC).build();
    }

    @Bean
    public DirectExchange productEsSyncFailedExchange() {
        return new DirectExchange(PRODUCT_ES_SYNC_FAILED_EXCHANGE);
    }

    @Bean
    public Queue productEsSyncFailedQueue() {
        return QueueBuilder.durable(PRODUCT_ES_SYNC_FAILED_QUEUE).build();
    }

    /**
     * 绑定短信队列到直连交换机
     */
    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue()).to(directExchange()).with(SEND_SMS_ROUTING_KEY);
    }

    /**
     * 绑定订单取消队列到延迟交换机
     */
    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(delayExchange()).with(ORDER_CANCEL_ROUTING_KEY).noargs();
    }

    /**
     * 绑定自动回复队列到延迟交换机
     */
    @Bean
    public Binding autoReplyBinding() {
        return BindingBuilder.bind(autoReplyQueue()).to(delayExchange()).with(AUTO_MESSAGE_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding orderConfirmReceiptBinding() {
        return BindingBuilder
                .bind(orderConfirmReceiptQueue())
                .to(delayExchange())
                .with(ORDER_CONFIRM_RECEIPT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding storeSettlementBinding() {
        return BindingBuilder
                .bind(storeSettlementQueue())
                .to(delayExchange())
                .with(STORE_SETTLEMENT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding aftersalesAutoReceiptBinding() {
        return BindingBuilder
                .bind(aftersalesAutoReceiptQueue())
                .to(delayExchange())
                .with(AFTERSALES_AUTO_RECEIPT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding autoReturnGoodBinding() {
        return BindingBuilder
                .bind(autoReturnGoodsQueue())
                .to(delayExchange())
                .with(AUTO_RETURN_GOODS_QUEUE_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding generateStatementBinding() {
        return BindingBuilder.bind(generateStatementQueue()).to(delayExchange()).with(GENERATE_STATEMENT_ROUTING_KEY).noargs();
    }

    @Bean
    public Binding translationNameBinding() {
        return BindingBuilder.bind(translationNameQueue()).to(directExchange()).with(TRANSLATION_NAME_ROUTING_KEY);
    }

    @Bean
    public Binding salesmanSettlementBinding() {
        return BindingBuilder
                .bind(salesmanSettlementQueue())
                .to(delayExchange())
                .with(SALESMAN_SETTLEMENT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding salesmanUpdateBinding() {
        return BindingBuilder.bind(salesmanUpdateQueue()).to(directExchange()).with(SALESMAN_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding vipUpdateBinding() {
        return BindingBuilder.bind(vipUpdateQueue()).to(directExchange()).with(VIP_UPDATE_ROUTING_KEY);
    }

    /**
     * 打印订单绑定
     */
    @Bean
    public Binding printOrderBinding() {
        return BindingBuilder.bind(printOrderQueue()).to(directExchange()).with(PRINT_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding vendorDiscontinueBinding() {
        return BindingBuilder.bind(vendorDiscontinueQueue()).to(directExchange()).with(VENDOR_DISCONTINUE_ROUTING_KEY);
    }

    @Bean
    public Binding vendorSettlementBinding() {
        return BindingBuilder.bind(vendorSettlementQueue()).to(directExchange()).with(VENDOR_SETTLEMENT_ROUTING_KEY);
    }

    @Bean
    public Binding refundSettlementBinding() {
        return BindingBuilder.bind(refundSettlementQueue()).to(directExchange()).with(REFUND_SETTLEMENT_ROUTING_KEY);
    }

    @Bean
    public Binding orderCompleteSendPointBinding() {
        return BindingBuilder
                .bind(orderCompleteSendPointQueue())
                .to(delayExchange())
                .with(ORDER_COMPLETE_SEND_POINT_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding pickupAutoStockBinding() {
        return BindingBuilder
                .bind(pickupAutoStockQueue())
                .to(delayExchange())
                .with(PICKUP_AUTO_STOCK_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding batchSyncBinding() {
        return BindingBuilder.bind(batchSyncQueue()).to(directExchange()).with(BATCH_SYNC_ROUTING_KEY);
    }

    @Bean
    public Binding singleSyncBinding() {
        return BindingBuilder.bind(singleSyncQueue()).to(directExchange()).with(SINGLE_SYNC_ROUTING_KEY);
    }

    @Bean
    public Binding productEsSyncFailedBinding() {
        return BindingBuilder.bind(productEsSyncFailedQueue())
                .to(productEsSyncFailedExchange())
                .with(PRODUCT_ES_SYNC_FAILED_ROUTING_KEY);
    }


    /**
     * 设置消息转换器，使用 JSON 格式
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        template.setRetryTemplate(RetryTemplate.builder()
                .maxAttempts(3)
                .exponentialBackoff(500, 2.0, 2000)
                .build());
        // 配置消息确认回调（用于检查消息是否正确到达交换机）
        template.setConfirmCallback((correlationData, ack, cause)
                -> System.out.println("消息确认状态：" + ack + ", 失败原因：" + (cause != null ? cause : "无")));

        // 配置返回回调（如果消息无法投递到队列）
        template.setReturnsCallback(returnedMessage
                -> System.out.println("消息无法路由到队列：" + new String(returnedMessage.getMessage().getBody(), StandardCharsets.UTF_8)));
        template.setMessageConverter(messageConverter());
        return template;
    }

    /**
     * 商品 ES 消费失败时最多执行三次，仍失败则保留到失败队列，避免消息被吞或无限热循环。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory productEsRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            RabbitTemplate rabbitTemplate) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        RepublishMessageRecoverer recoverer = new RepublishMessageRecoverer(
                rabbitTemplate,
                PRODUCT_ES_SYNC_FAILED_EXCHANGE,
                PRODUCT_ES_SYNC_FAILED_ROUTING_KEY
        );
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 5000)
                .recoverer(recoverer)
                .build());
        return factory;
    }

    // 使用 JSON 格式转换消息
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
