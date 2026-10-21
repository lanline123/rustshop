package com.tigshop.common.config;

//**---------------------------------------------------------------------+
//** 配置文件 -- Swagger3.0配置
//**---------------------------------------------------------------------+
//** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
//**---------------------------------------------------------------------+
//** 作者：Tigshop团队，yq@tigshop.com
//**---------------------------------------------------------------------+
//** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
//**---------------------------------------------------------------------+

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * @author jayce
 * @create 2024/09/24
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenApi() {
        /*
         *  作者名称
         *  作者邮箱
         *  介绍作者的URL地址
         *  使用Map配置信息（如key为"name","email","url"）
         */
        Contact contact = new Contact()
                .name("TigShop团队")
                .email("yq@tigshop.com")
                .url("https://www.tigshop.com")
                .extensions(Collections.emptyMap());

        /*
         * Api接口文档标题（必填）
         * Api接口文档描述
         * Api接口版本
         * 设置联系人信息
         */
        Info info = new Info()
                .title("tigshop接口文档")
                .description("Tigshop-Java 开发框架\n" +
                        "采用最前沿、活跃度最高的技术栈")
                .version("1.0.0")
                .contact(contact);
        /*
         * 配置Swagger3.0描述信息
         */
        return new OpenAPI()
                .info(info);
    }
}