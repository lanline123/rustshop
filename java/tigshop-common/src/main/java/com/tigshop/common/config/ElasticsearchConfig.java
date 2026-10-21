// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchConfig {

    /**
     * ES主机地址
     */
    private String host;

    /**
     * ES端口
     */
    private Integer port;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 是否启用SSL验证
     */
    private Boolean sslVerify;

    /**
     * 连接超时时间（秒）
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retries;

    /**
     * 产品索引名称
     */
    private String productIndex;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建ElasticsearchClient Bean
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        // 创建RestClient
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(host, port, sslVerify ? "https" : "http")
        );

        // 设置认证信息
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // 设置超时和重试
        builder.setRequestConfigCallback(requestConfigBuilder ->
                requestConfigBuilder.setConnectTimeout(timeout * 1000)
                        .setSocketTimeout(timeout * 1000));

        RestClient restClient = builder.build();

        // 创建传输层，使用Spring Boot管理的ObjectMapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));

        // 创建客户端
        return new ElasticsearchClient(transport);
    }
}
