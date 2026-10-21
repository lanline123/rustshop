package com.tigshop.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Tigshop团队
 */
@ConfigurationPropertiesScan("com.tigshop.common.properties")
@SpringBootApplication(scanBasePackages = {"com.tigshop"})
@MapperScan(basePackages = {"com.tigshop.mapper"})
@EnableFeignClients("com.tigshop.feign")
@EnableRabbit
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
