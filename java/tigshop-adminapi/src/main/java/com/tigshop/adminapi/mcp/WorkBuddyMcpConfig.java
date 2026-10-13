package com.tigshop.adminapi.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Tigshop MCP 远程端点。
 *
 * <p>面向支持 MCP 的 AI 客户端暴露工具，客户端只需配置 URL 与密钥，无需在本机安装任何程序。
 * 非 AI 的通用集成方仍走 {@code /adminapi/openapi/mcp/v1} 的 REST 接口，两者共用同一套 service。
 *
 * <p>选用无状态传输而非 Streamable：MCP 层不保留会话，多实例部署无需粘性会话，
 * 与既有开放接口的无状态设计一致。
 *
 * <p>端点保留 {@code /adminapi} 前缀，原因同 WorkBuddyProductImportController：
 * 网关只把该前缀转发到本服务，其余路径会落到前台站点（CR-20260827-164700 FIX-05）。
 *
 * <p>依赖选型见父 pom：Spring AI 各版本要求的 Spring Boot 均高于本项目的 3.3.4，
 * 故直接使用 MCP 官方 SDK 自带的 Servlet 传输。
 */
@Configuration
public class WorkBuddyMcpConfig {

    public static final String MCP_ENDPOINT = "/adminapi/mcp";

    /** 传输层把密钥放进上下文的键，供工具处理器取回鉴权。 */
    static final String CONTEXT_API_KEY = "workbuddyApiKey";

    /** 站点基址，用于在结果摘要里给出后台商品列表的可点击地址。 */
    static final String CONTEXT_BASE_URL = "workbuddyBaseUrl";

    /**
     * Servlet 请求属性，供工具线程重新绑定。
     *
     * <p>工具在 reactor 线程执行，而下游（如 ProductServiceImpl.create）经 HeaderUtils 读请求头，
     * 取不到请求上下文时会抛 SYSTEM_ERROR 并回滚事务，使导入恒失败且不留数据。
     */
    static final String CONTEXT_REQUEST = "workbuddyRequestAttributes";

    private static final String KEY_HEADER = "X-MCP-Api-Key";

    @Bean
    public HttpServletStatelessServerTransport workBuddyMcpTransport(ObjectMapper objectMapper) {
        return HttpServletStatelessServerTransport.builder()
                .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
                .messageEndpoint(MCP_ENDPOINT)
                .contextExtractor(request -> {
                    Map<String, Object> attributes = new HashMap<>();
                    String key = request.getHeader(KEY_HEADER);
                    if (key != null) {
                        attributes.put(CONTEXT_API_KEY, key);
                    }
                    attributes.put(CONTEXT_BASE_URL, baseUrl(request));
                    attributes.put(CONTEXT_REQUEST, new ServletRequestAttributes(request));
                    return McpTransportContext.create(attributes);
                })
                .build();
    }

    /** 取客户端visible的站点地址：网关会透传 Host，故以请求信息为准而非本机监听地址。 */
    private static String baseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean standardPort = ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
        StringBuilder url = new StringBuilder(scheme).append("://").append(request.getServerName());
        if (!standardPort && port > 0) {
            url.append(':').append(port);
        }
        return url.toString();
    }

    @Bean
    public ServletRegistrationBean<HttpServletStatelessServerTransport> workBuddyMcpServlet(
            HttpServletStatelessServerTransport transport) {
        ServletRegistrationBean<HttpServletStatelessServerTransport> registration =
                new ServletRegistrationBean<>(transport, MCP_ENDPOINT);
        registration.setName("workBuddyMcpServlet");
        return registration;
    }

    @Bean
    public McpStatelessSyncServer workBuddyMcpServer(HttpServletStatelessServerTransport transport,
                                                     WorkBuddyMcpTools tools) {
        return McpServer.sync(transport)
                .serverInfo("tigshop-mcp", "1.1.0")
                .instructions(WorkBuddyAssistantGuide.INSTRUCTIONS)
                .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
                .toolCall(WorkBuddyMcpTools.CONNECTION_STATUS, tools::connectionStatus)
                .toolCall(WorkBuddyMcpTools.PRODUCT_QUERY, tools::productQuery)
                .toolCall(WorkBuddyMcpTools.PRODUCT_PREPARE, tools::productPrepare)
                .toolCall(WorkBuddyMcpTools.PRODUCT_EXECUTE, tools::productExecute)
                .toolCall(WorkBuddyMcpTools.PRODUCT_STATUS, tools::productStatus)
                .toolCall(WorkBuddyMcpTools.PRODUCT_COPY, tools::productCopy)
                .toolCall(WorkBuddyMcpTools.CATEGORY_QUERY, tools::categoryQuery)
                .toolCall(WorkBuddyMcpTools.CAPABILITIES, tools::capabilities)
                .toolCall(WorkBuddyMcpTools.MEDIA_UPLOAD, tools::mediaUpload)
                .toolCall(WorkBuddyMcpTools.MEDIA_QUERY, tools::mediaQuery)
                .toolCall(WorkBuddyMcpTools.MEDIA_PREPARE, tools::mediaPrepare)
                .toolCall(WorkBuddyMcpTools.MEDIA_EXECUTE, tools::mediaExecute)
                .toolCall(WorkBuddyMcpTools.CATEGORY_PREPARE, tools::categoryPrepare)
                .toolCall(WorkBuddyMcpTools.CATEGORY_EXECUTE, tools::categoryExecute)
                .toolCall(WorkBuddyMcpTools.PRODUCT_ANALYSIS, tools::productAnalysis)
                .build();
    }
}
