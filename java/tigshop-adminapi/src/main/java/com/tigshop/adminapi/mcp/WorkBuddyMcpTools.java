package com.tigshop.adminapi.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tigshop.bean.dto.workbuddy.MediaUploadFileDTO;
import com.tigshop.bean.dto.workbuddy.MediaUploadRequestDTO;
import com.tigshop.bean.dto.workbuddy.MediaQueryDTO;
import com.tigshop.bean.dto.workbuddy.MediaOperationDTO;
import com.tigshop.bean.dto.workbuddy.CategoryImportDTO;
import com.tigshop.bean.dto.workbuddy.ProductAnalysisDTO;
import com.tigshop.bean.dto.workbuddy.ProductDraftDTO;
import com.tigshop.bean.dto.workbuddy.ProductSearchDTO;
import com.tigshop.bean.vo.workbuddy.*;
import com.tigshop.service.workbuddy.McpMediaUploadService;
import com.tigshop.service.workbuddy.MediaManagementService;
import com.tigshop.service.workbuddy.CategoryImportService;
import com.tigshop.service.workbuddy.ProductAnalysisService;
import com.tigshop.service.workbuddy.ProductImportService;
import com.tigshop.service.workbuddy.ProductContextService;
import com.tigshop.service.workbuddy.ProductManageService;
import com.tigshop.service.workbuddy.WorkBuddyKeyService;
import com.tigshop.service.workbuddy.WorkBuddyApiException;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.helper.SettingsHelper;
import com.tigshop.bean.enums.setting.SettingsEnum;
import cn.hutool.core.util.StrUtil;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.tigshop.common.constant.HttpStatus.UNAUTHORIZED;

/** MCP 业务域工具门面：对 AI 暴露少量 query/prepare/execute 工具，底层 service 保持不变。 */
@Component
public class WorkBuddyMcpTools {
    private static final Logger log = LoggerFactory.getLogger(WorkBuddyMcpTools.class);
    private static final Map<String, Object> NO_ARGS = Map.of("type", "object", "properties", Map.of(), "additionalProperties", false);

    static final McpSchema.Tool CONNECTION_STATUS = tool("tigshop_connection_status", "检查 Tigshop MCP 连接和密钥状态", Map.of());
    static final McpSchema.Tool PRODUCT_QUERY = tool("tigshop_product_query", "查询商品：搜索、详情或推广链接由 operation 区分；创建商品前必须用 context 读取当前商城编码习惯", queryProperties(), "operation");
    static final McpSchema.Tool PRODUCT_PREPARE = tool("tigshop_product_prepare", "用自然语言引导新建商品，或校验/预览商品操作；不输出固定客户模板，也不修改商品。创建前先调用 product_query(context) 获取编码候选", prepareProperties(), "operation");
    static final McpSchema.Tool PRODUCT_EXECUTE = tool("tigshop_product_execute", "执行商品新建、修改、回收、还原、删除或批量操作；create 必须携带 validate 返回的 previewId、唯一 requestId 和 confirmation: true；必须展示最终草稿并取得用户对具体动作的明确确认，模糊回复不算确认", executeProperties(), "operation");
    static final McpSchema.Tool PRODUCT_STATUS = tool("tigshop_product_status", "上架或下架商品；上架前必须检查审核状态，审核未通过不得承诺上架；必须明确 confirmation: true", Map.of("productId", integerSchema(), "onSale", Map.of("type", "boolean"), "confirmation", Map.of("type", "boolean", "const", true)), "productId", "onSale", "confirmation");
    static final McpSchema.Tool PRODUCT_COPY = tool("tigshop_product_copy", "复制商品并生成新的商品货号；必须明确 confirmation: true", Map.of("productId", integerSchema(), "confirmation", Map.of("type", "boolean", "const", true)), "productId", "confirmation");
    static final McpSchema.Tool CATEGORY_QUERY = tool("tigshop_category_query", "按名称检索或逐级浏览商品分类；展示完整路径，带下级的分类不能直接用于商品创建", Map.of("keyword", Map.of("type", "string"), "parentId", integerSchema()));
    static final McpSchema.Tool CAPABILITIES = tool("tigshop_capabilities", "返回当前商品助手支持的能力、字段约束、素材限制和全局指引", Map.of());
    static final McpSchema.Tool MEDIA_UPLOAD = tool("tigshop_media_upload", "严格按唯一链路批量上传图片或视频：每个文件必须先通过 multipart media/upload-token 获取短时 fileToken，再传 fileToken；禁止 Base64、本地路径和其他上传方式，缺少 token 立即停止；必须取得用户的明确确认并传 confirmation: true", mediaUploadProperties(), "files", "confirmation");
    static final McpSchema.Tool MEDIA_QUERY = tool("tigshop_media_query", "查询素材、素材详情或素材分组", mediaQueryProperties(), "operation");
    static final McpSchema.Tool MEDIA_PREPARE = tool("tigshop_media_prepare", "预览素材上传、修改、删除或分组操作；不产生写入", mediaOperationProperties(), "operation", "requestId");
    static final McpSchema.Tool MEDIA_EXECUTE = tool("tigshop_media_execute", "执行已确认的素材操作；删除素材不可恢复，必须展示素材名称/ID并取得明确确认", mediaOperationProperties(), "operation", "confirmation", "previewId", "requestId");
    static final McpSchema.Tool CATEGORY_PREPARE = tool("tigshop_category_prepare", "预览分类树导入并检查重复及父子层级；不产生写入", categoryImportProperties(), "operation", "requestId");
    static final McpSchema.Tool CATEGORY_EXECUTE = tool("tigshop_category_execute", "执行已确认的分类树导入", categoryImportProperties(), "operation", "confirmation", "previewId", "requestId");
    static final McpSchema.Tool PRODUCT_ANALYSIS = tool("tigshop_product_analysis", "分析销量、库存、售价和品类并给出可解释建议（只读）", productAnalysisProperties(), "operation");

    @Resource private WorkBuddyKeyService keyService;
    @Resource private ProductImportService importService;
    @Resource private ProductManageService productManageService;
    @Resource private ProductContextService productContextService;
    @Resource private McpMediaUploadService mcpMediaUploadService;
    @Resource private MediaManagementService mediaManagementService;
    @Resource private CategoryImportService categoryImportService;
    @Resource private ProductAnalysisService productAnalysisService;
    @Value("${spring.servlet.multipart.max-file-size:50MB}") private String multipartMaxFileSize;
    @Resource private SettingsHelper settingsHelper;
    @Resource private ObjectMapper objectMapper;

    McpSchema.CallToolResult connectionStatus(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> ok(keyService.status(apiKey(c), null), "连接状态已返回")); }

    McpSchema.CallToolResult productQuery(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> {
        Map<String, Object> a = args(r); String op = text(a.get("operation")); Integer shop = authenticate(c, "product:read");
        if ("search".equals(op)) { Map<String, Object> searchArgs = new LinkedHashMap<>(a); searchArgs.remove("operation"); ProductSearchVO v = productManageService.search(objectMapper.convertValue(searchArgs, ProductSearchDTO.class), shop); return ok(v, WorkBuddyProductSummaries.search(v)); }
        Integer id = integer(a.get("productId"));
        if ("detail".equals(op)) { ProductDetailVO v = productManageService.detail(id, shop); return ok(v, WorkBuddyProductSummaries.detail(v)); }
        if ("promotion_links".equals(op)) { PromotionLinksVO v = productManageService.promotionLinks(id, shop); return ok(v, WorkBuddyProductSummaries.promotion(v)); }
        if ("context".equals(op)) {
            if (productContextService == null) return error("SERVICE_UNAVAILABLE", "商品上下文服务暂不可用");
            ProductContextVO v = productContextService.context(text(a.get("keyword")), integer(a.get("categoryId")), integer(a.get("sampleSize")), shop);
            return ok(v, WorkBuddyProductSummaries.context(v));
        }
        return error("INVALID_ARGUMENT", "operation 只能是 search、detail、promotion_links 或 context");
    }); }

    McpSchema.CallToolResult productPrepare(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> {
        Map<String, Object> a = args(r); String op = text(a.get("operation"));
        Integer shop = "requirements".equals(op) ? authenticate(c, "product:read") : authenticate(c, "product:write");
        if ("requirements".equals(op)) { ProductRequirementsVO v = importService.requirements(); return ok(v, WorkBuddyMcpSummaries.requirements(v)); }
        if ("validate".equals(op)) { List<ProductDraftDTO> drafts = objectMapper.convertValue(a.get("products"), new TypeReference<>() {}); ProductValidationVO v = importService.validate(drafts, shop); return ok(v, WorkBuddyMcpSummaries.validation(v, drafts)); }
        if ("batch_preview".equals(op)) { List<Integer> ids = objectMapper.convertValue(a.get("productIds"), new TypeReference<>() {}); BatchPreviewVO v = productManageService.previewBatch(text(a.get("batchOperation")), ids, shop); return ok(v, WorkBuddyProductSummaries.preview(v)); }
        return error("INVALID_ARGUMENT", "operation 只能是 requirements、validate 或 batch_preview");
    }); }

    McpSchema.CallToolResult productExecute(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> {
        Map<String, Object> a = args(r); String op = text(a.get("operation")); Integer shop = authenticate(c, "product:write");
        if (!isTrue(a.get("confirmation"))) return error("CONFIRMATION_REQUIRED", "写入前必须明确 confirmation: true");
        if ("create".equals(op)) { List<ProductDraftDTO> drafts = objectMapper.convertValue(a.get("products"), new TypeReference<>() {}); ProductImportResultVO v = importService.importProducts(drafts, shop, text(a.get("previewId")), text(a.get("requestId")), true); return ok(v, WorkBuddyMcpSummaries.importResult(v, baseUrl(c))); }
        Integer id = integer(a.get("productId"));
        if ("update".equals(op)) return ok(productManageService.updateField(id, text(a.get("field")), a.get("value"), shop), "商品字段已更新");
        if ("recycle".equals(op)) return ok(productManageService.recycle(id, shop), "商品已移入回收站");
        if ("restore".equals(op)) return ok(productManageService.restore(id, shop), "商品已从回收站还原");
        if ("delete".equals(op)) { if (!isTrue(a.get("confirmation"))) return error("CONFIRMATION_REQUIRED", "删除不可恢复，请明确 confirmation: true"); return ok(productManageService.deletePermanently(id, shop), "商品已彻底删除"); }
        if ("batch".equals(op)) { List<Integer> ids = objectMapper.convertValue(a.get("productIds"), new TypeReference<>() {}); BatchResultVO v = productManageService.executeBatch(text(a.get("batchOperation")), ids, text(a.get("previewId")), isTrue(a.get("confirmation")), shop); return ok(v, WorkBuddyProductSummaries.batch(v)); }
        return error("INVALID_ARGUMENT", "operation 只能是 create、update、recycle、restore、delete 或 batch");
    }); }

    McpSchema.CallToolResult productStatus(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> { Map<String, Object> a = args(r); if (!isTrue(a.get("confirmation"))) return error("CONFIRMATION_REQUIRED", "写入前必须明确 confirmation: true"); return ok(productManageService.setStatus(integer(a.get("productId")), isTrue(a.get("onSale")), authenticate(c, "product:write")), "商品状态已更新"); }); }
    McpSchema.CallToolResult productCopy(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> { if (!isTrue(args(r).get("confirmation"))) return error("CONFIRMATION_REQUIRED", "复制前必须明确 confirmation: true"); return ok(productManageService.copy(integer(args(r).get("productId")), authenticate(c, "product:write")), "商品已复制"); }); }
    McpSchema.CallToolResult categoryQuery(McpTransportContext c, McpSchema.CallToolRequest r) { return guard(c, r, () -> { Map<String, Object> a = args(r); Integer shop = authenticate(c, "product:read"); List<ProductCategoryVO> v = importService.categories(text(a.get("keyword")), integer(a.get("parentId"))); return ok(Map.of("categories", v, "total", v.size()), WorkBuddyMcpSummaries.categories(v, text(a.get("keyword")))); }); }
    McpSchema.CallToolResult capabilities(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> {
            authenticate(c, "product:read");
            McpCapabilitiesVO value = new McpCapabilitiesVO();
            long multipartBytes = parseSize(multipartMaxFileSize, value.getMedia().getMaxFileBytes());
            value.getMedia().setMaxFileBytes(multipartBytes);
            value.getMedia().setVideoMaxFileBytes(multipartBytes);
            long imageBytes = multipartBytes;
            try {
                java.math.BigDecimal configured = settingsHelper == null ? null : settingsHelper.get(SettingsEnum.IMAGE_UPLOAD_MAX_SIZE);
                if (configured != null && configured.signum() > 0) imageBytes = Math.min(multipartBytes, configured.multiply(java.math.BigDecimal.valueOf(1048576L)).longValue());
            } catch (Exception ignored) { }
            value.getMedia().setImageMaxFileBytes(imageBytes);
            value.setAssistant(WorkBuddyAssistantGuide.capabilities());
            // stageEndpoint/tokenTtl 等通用附件协商字段由 VO 默认值提供，确保所有 MCP 客户端
            // 都能发现 fileToken 优先策略，而不依赖某个客户端的私有约定。
            String stageEndpoint = (baseUrl(c) == null ? "" : baseUrl(c)) + value.getMedia().getStageEndpoint();
            return ok(value, "当前支持商品创建与管理、素材中心管理、分类批量导入和商品分析推荐能力。媒体上传严格只允许 fileToken：先用 multipart/form-data 调用 "
                    + stageEndpoint + " 获取短时 fileToken，再调用 tigshop_media_upload；禁止 Base64、本地路径和其他上传方式，缺少 fileToken 时立即停止并提示升级客户端。图片参与商品创建时请使用支持图片识别的模型。");
        });
    }
    McpSchema.CallToolResult mediaUpload(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> {
            Integer shop = authenticate(c, "product:write");
            Map<String, Object> a = args(r);
            if (!isTrue(a.get("confirmation"))) return error("CONFIRMATION_REQUIRED", "上传素材前必须明确 confirmation: true");
            if (mcpMediaUploadService == null) return error("SERVICE_UNAVAILABLE", "素材上传服务暂不可用");
            MediaUploadRequestDTO request = new MediaUploadRequestDTO();
            request.setConfirmation(true);
            List<MediaUploadFileDTO> files = objectMapper.convertValue(a.get("files"), new TypeReference<List<MediaUploadFileDTO>>() {});
            Integer defaultGroupId = integer(a.get("groupId"));
            if (defaultGroupId != null) files.forEach(file -> { if (file.getGroupId() == null) file.setGroupId(defaultGroupId); });
            request.setFiles(files);
            if (files.stream().anyMatch(file -> StrUtil.isBlank(file.getFileToken()))) {
                return error("UPLOAD_MODE_REQUIRED", "媒体上传只允许 fileToken；请先通过 multipart media/upload-token 获取短时令牌，禁止使用 Base64、本地路径或其他上传方式");
            }
            MediaUploadResultVO result = mcpMediaUploadService.uploadByTokens(request, shop);
            String summary = result == null || result.getSummary() == null ? "素材上传已完成" : result.getSummary();
            return ok(result, summary + " 当前任务包含图片或视频；创建商品前请确认使用支持图片识别的模型。接下来可以：用这些素材生成商品草稿、查看素材预览或调整素材分组。你想继续哪一项？");
        });
    }
    McpSchema.CallToolResult mediaQuery(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> {
            if (mediaManagementService == null) return error("SERVICE_UNAVAILABLE", "素材查询服务暂不可用");
            Integer shop = authenticate(c, "media:read");
            MediaQueryDTO query = objectMapper.convertValue(args(r), MediaQueryDTO.class);
            Map<String, Object> result = mediaManagementService.query(query, shop);
            return ok(result, WorkBuddyMcpSummaries.mediaQuery(result, query.getOperation()));
        });
    }
    McpSchema.CallToolResult mediaPrepare(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> { if (mediaManagementService == null) return error("SERVICE_UNAVAILABLE", "素材操作服务暂不可用"); Map<String,Object> a=args(r); if ("upload".equals(text(a.get("operation")))) return error("UPLOAD_MODE_REQUIRED", "媒体上传只能使用 tigshop_media_upload 的 fileToken 单一路径"); Integer shop=authenticate(c,"media:write"); return ok(mediaManagementService.prepare(objectMapper.convertValue(a, MediaOperationDTO.class), shop), "素材操作预览已生成"); });
    }
    McpSchema.CallToolResult mediaExecute(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> { if (mediaManagementService == null) return error("SERVICE_UNAVAILABLE", "素材操作服务暂不可用"); Map<String,Object> a=args(r); if ("upload".equals(text(a.get("operation")))) return error("UPLOAD_MODE_REQUIRED", "媒体上传只能使用 tigshop_media_upload 的 fileToken 单一路径"); if (!isTrue(a.get("confirmation"))) return error("CONFIRMATION_REQUIRED", "素材写入前必须明确 confirmation: true"); Integer shop=authenticate(c,"media:write"); return ok(mediaManagementService.execute(objectMapper.convertValue(a, MediaOperationDTO.class), shop), "素材操作已执行"); });
    }
    McpSchema.CallToolResult categoryPrepare(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> { if (categoryImportService == null) return error("SERVICE_UNAVAILABLE", "分类导入服务暂不可用"); Map<String,Object> a=args(r); Integer shop=authenticate(c,"category:write"); return ok(categoryImportService.prepare(objectMapper.convertValue(a, CategoryImportDTO.class), shop), "分类导入预览已生成"); });
    }
    McpSchema.CallToolResult categoryExecute(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> { if (categoryImportService == null) return error("SERVICE_UNAVAILABLE", "分类导入服务暂不可用"); Map<String,Object> a=args(r); if (!isTrue(a.get("confirmation"))) return error("CONFIRMATION_REQUIRED", "分类写入前必须明确 confirmation: true"); Integer shop=authenticate(c,"category:write"); return ok(categoryImportService.execute(objectMapper.convertValue(a, CategoryImportDTO.class), shop), "分类导入已执行"); });
    }
    McpSchema.CallToolResult productAnalysis(McpTransportContext c, McpSchema.CallToolRequest r) {
        return guard(c, r, () -> { if (productAnalysisService == null) return error("SERVICE_UNAVAILABLE", "商品分析服务暂不可用"); Integer shop=authenticate(c,"analysis:read"); return ok(productAnalysisService.analyze(objectMapper.convertValue(args(r), ProductAnalysisDTO.class), shop), "商品分析已完成"); });
    }

    private Integer authenticate(McpTransportContext c, String scope) {
        String key = apiKey(c);
        WorkBuddyKeyStatusVO s = keyService.status(key, null);
        if (s == null || !s.isValid() || s.getShopId() == null) {
            // 鉴权失败必须保留机器可读的 401 语义，避免被 guard 误报为工具内部错误。
            throw new WorkBuddyApiException("UNAUTHORIZED", safeAuthMessage(s), UNAUTHORIZED, false);
        }
        try {
            keyService.requireValid(key, s.getShopId(), scope);
        } catch (GlobalException e) {
            if (UNAUTHORIZED == e.getCode()) {
                throw new WorkBuddyApiException("UNAUTHORIZED", safeAuthMessage(e.getMessage()), UNAUTHORIZED, false);
            }
            throw e;
        }
        return s.getShopId();
    }

    private static String safeAuthMessage(WorkBuddyKeyStatusVO status) {
        return safeAuthMessage(status == null ? null : status.getMessage());
    }

    private static String safeAuthMessage(String message) {
        if ("MCP密钥已过期".equals(message)
                || "MCP密钥缺失或未配置".equals(message)
                || "MCP密钥到期配置无效".equals(message)
                || "MCP密钥无效".equals(message)) {
            return message;
        }
        return "MCP密钥无效";
    }
    private McpSchema.CallToolResult guard(McpTransportContext c, McpSchema.CallToolRequest r, Supplier<McpSchema.CallToolResult> action) {
        RequestAttributes inherited = c.get(WorkBuddyMcpConfig.CONTEXT_REQUEST) instanceof RequestAttributes a ? a : null;
        String key = apiKey(c);
        try {
            McpSchema.CallToolResult result;
            if (inherited instanceof ServletRequestAttributes s) result = WorkBuddyRequestContext.runAsPlatformAdmin(s.getRequest(), action);
            else result = action.get();
            return result;
        } catch (WorkBuddyApiException e) {
            if (UNAUTHORIZED == e.getCode()) {
                McpSchema.CallToolResult unauthorized = error("UNAUTHORIZED", safeAuthMessage(e.getMessage()));
                return unauthorized;
            }
            log.warn("MCP 工具调用失败 tool={} error={}", r.name(), safeToolMessage(e.getMessage()));
            McpSchema.CallToolResult failed = error("TOOL_CALL_FAILED", safeToolMessage(e.getMessage()));
            return failed;
        } catch (Exception e) {
            log.warn("MCP 工具调用失败 tool={} error={}", r.name(), safeToolMessage(e.getMessage()));
            McpSchema.CallToolResult failed = error("TOOL_CALL_FAILED", safeToolMessage(e.getMessage()));
            return failed;
        }
    }


    /**
     * MCP 失败响应会直接返回给外部 AI 客户端，不能把下游异常中的本地路径、
     * Base64 或凭证片段原样透出。日志复用同一套脱敏规则，保留可诊断的短消息。
     */
    private static String safeToolMessage(String message) {
        String safe = message == null ? null : message
                .replaceAll("(?i)(authorization\\s*[:=]\\s*bearer\\s+)[^\\s,;]+", "$1[REDACTED]")
                .replaceAll("(?i)(api[-_ ]?key\\s*[:=]\\s*)[^\\s,;]+", "$1[REDACTED]")
                .replaceAll("(?i)(password|secret|token)(\\s*[:=]\\s*)[^\\s,;]+", "$1$2[REDACTED]");
        if (safe == null || safe.isBlank()) return "服务内部错误";
        return safe.length() > 500 ? safe.substring(0, 500) : safe;
    }


    /**
     * 返回一个面向用户的摘要，同时把完整载荷放在 structuredContent 供客户端继续处理。
     * 不再把 JSON 作为第二段文本返回，避免 WorkBuddy 等客户端重复展示业务结果。
     */
    private McpSchema.CallToolResult ok(Object payload, String summary) { return McpSchema.CallToolResult.builder().structuredContent(payload).addTextContent(summary).build(); }
    private McpSchema.CallToolResult error(String code, String message) { return McpSchema.CallToolResult.builder().isError(true).structuredContent(Map.of("errorCode", code, "message", message == null ? "调用失败" : message)).addTextContent(message == null ? "调用失败" : message).build(); }
    private static Map<String, Object> args(McpSchema.CallToolRequest r) { return r.arguments() == null ? Map.of() : r.arguments(); }
    private static String apiKey(McpTransportContext c) { return text(c.get(WorkBuddyMcpConfig.CONTEXT_API_KEY)); }
    private static String baseUrl(McpTransportContext c) { return text(c.get(WorkBuddyMcpConfig.CONTEXT_BASE_URL)); }
    private static String text(Object v) { return v == null ? null : String.valueOf(v); }
    private static Integer integer(Object v) { try { return v == null ? null : v instanceof Number n ? n.intValue() : Integer.valueOf(String.valueOf(v)); } catch (Exception e) { return null; } }
    private static boolean isTrue(Object v) { return Boolean.TRUE.equals(v) || "true".equals(v); }
    private static long parseSize(String value, long fallback) { try { if (value == null) return fallback; String raw = value.trim().toUpperCase(); if (raw.endsWith("GB")) return Long.parseLong(raw.substring(0, raw.length() - 2).trim()) * 1073741824L; if (raw.endsWith("MB")) return Long.parseLong(raw.substring(0, raw.length() - 2).trim()) * 1048576L; if (raw.endsWith("KB")) return Long.parseLong(raw.substring(0, raw.length() - 2).trim()) * 1024L; return Long.parseLong(raw); } catch (Exception ignored) { return fallback; } }
    private static Map<String, Object> integerSchema() { return Map.of("type", "integer", "minimum", 1); }
    private static Map<String, Object> queryProperties() { Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "enum", List.of("search", "detail", "promotion_links", "context"))); p.put("productId", integerSchema()); p.put("keyword", Map.of("type", "string")); p.put("scope", Map.of("type", "string", "enum", List.of("on_sale", "off_sale", "pending_audit", "audit_failed", "recycled", "all"))); p.put("categoryId", integerSchema()); p.put("sampleSize", Map.of("type", "integer", "minimum", 1, "maximum", 100, "description", "context 会返回历史编码格式、候选下一个编码及默认字段建议")); p.put("brandId", integerSchema()); p.put("productType", integerSchema()); p.put("minPrice", Map.of("type", "number", "minimum", 0)); p.put("maxPrice", Map.of("type", "number", "minimum", 0)); p.put("minStock", Map.of("type", "integer", "minimum", 0)); p.put("maxStock", Map.of("type", "integer", "minimum", 0)); p.put("minSales", Map.of("type", "integer", "minimum", 0)); p.put("maxSales", Map.of("type", "integer", "minimum", 0)); p.put("sortField", Map.of("type", "string")); p.put("sortOrder", Map.of("type", "string")); p.put("page", Map.of("type", "integer", "minimum", 1)); p.put("size", Map.of("type", "integer", "minimum", 1, "maximum", 100)); return p; }
     private static Map<String, Object> mediaUploadProperties() { Map<String, Object> file = new LinkedHashMap<>(); file.put("type", "object"); Map<String, Object> fp = new LinkedHashMap<>(); fp.put("fileToken", Map.of("type", "string", "minLength", 1, "description", "由 multipart media/upload-token 返回的短时令牌")); fp.put("role", Map.of("type", "string", "enum", List.of("main_image", "gallery_image", "detail_image", "video"))); fp.put("groupId", Map.of("type", "integer", "minimum", 0)); fp.put("clientFileId", Map.of("type", "string")); file.put("properties", fp); file.put("required", List.of("fileToken")); file.put("additionalProperties", false); Map<String, Object> files = new LinkedHashMap<>(); files.put("type", "array"); files.put("minItems", 1); files.put("maxItems", 20); files.put("items", file); Map<String, Object> p = new LinkedHashMap<>(); p.put("files", files); p.put("confirmation", Map.of("type", "boolean", "const", true)); p.put("groupId", Map.of("type", "integer", "minimum", 0, "description", "未在单文件指定时使用的默认素材分组")); return p; }
    private static Map<String, Object> mediaQueryProperties() { Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "enum", List.of("list", "detail", "groups"))); p.put("type", Map.of("type", "string", "enum", List.of("image", "video"), "description", "查询分组时选择图片或视频分组树；未传时兼容图片分组")); p.put("groupId", Map.of("type", "integer", "minimum", 0)); p.put("keyword", Map.of("type", "string")); p.put("page", Map.of("type", "integer", "minimum", 1)); p.put("pageSize", Map.of("type", "integer", "minimum", 1, "maximum", 100)); p.put("id", Map.of("type", "integer", "minimum", 1)); return p; }
    private static Map<String, Object> mediaOperationProperties() { Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "enum", List.of("update", "delete", "group_create", "group_update", "group_delete"))); Map<String, Object> item = new LinkedHashMap<>(); item.put("type", "object"); item.put("properties", Map.of("id", integerSchema(), "name", Map.of("type", "string"), "mimeType", Map.of("type", "string"), "groupId", Map.of("type", "integer", "minimum", 0), "role", Map.of("type", "string"))); item.put("additionalProperties", false); p.put("items", Map.of("type", "array", "maxItems", 50, "items", item)); p.put("id", integerSchema()); p.put("ids", Map.of("type", "array", "minItems", 1, "maxItems", 50, "uniqueItems", true, "items", integerSchema())); p.put("name", Map.of("type", "string", "minLength", 1, "maxLength", 200)); p.put("type", Map.of("type", "string", "enum", List.of("image", "video"), "description", "分组操作必填时选择图片或视频分组；未传时兼容图片分组")); p.put("groupId", Map.of("type", "integer", "minimum", 0)); p.put("previewId", Map.of("type", "string", "minLength", 1)); p.put("requestId", Map.of("type", "string", "minLength", 1)); p.put("confirmation", Map.of("type", "boolean", "const", true)); return p; }
    private static Map<String, Object> categoryImportProperties() { Map<String, Object> node = new LinkedHashMap<>(); node.put("type", "object"); node.put("properties", Map.of("id", integerSchema(), "name", Map.of("type", "string", "minLength", 1), "children", Map.of("type", "array", "items", Map.of("type", "object")))); node.put("additionalProperties", false); Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "const", "import")); p.put("nodes", Map.of("type", "array", "minItems", 1, "maxItems", 200, "items", node)); p.put("previewId", Map.of("type", "string", "minLength", 1)); p.put("requestId", Map.of("type", "string", "minLength", 1)); p.put("confirmation", Map.of("type", "boolean", "const", true)); return p; }
    private static Map<String, Object> productAnalysisProperties() { Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "enum", List.of("overview", "top_sellers", "slow_movers", "inventory_risk", "price_bands", "category_performance", "recommend"))); p.put("startTime", Map.of("type", "string", "format", "date-time")); p.put("endTime", Map.of("type", "string", "format", "date-time")); p.put("categoryId", integerSchema()); p.put("keyword", Map.of("type", "string")); p.put("sampleSize", Map.of("type", "integer", "minimum", 1, "maximum", 100)); p.put("page", Map.of("type", "integer", "minimum", 1)); return p; }
    private static Map<String, Object> prepareProperties() { Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "enum", List.of("requirements", "validate", "batch_preview"))); p.put("products", Map.of("type", "array", "minItems", 1, "maxItems", 50, "items", productDraftSchema())); p.put("productIds", Map.of("type", "array", "minItems", 1, "maxItems", 50, "items", integerSchema())); p.put("batchOperation", Map.of("type", "string", "enum", List.of("up", "down", "recycle", "restore", "del"))); return p; }
    private static Map<String, Object> executeProperties() { Map<String, Object> p = new LinkedHashMap<>(); p.put("operation", Map.of("type", "string", "enum", List.of("create", "update", "recycle", "restore", "delete", "batch"))); p.put("productId", integerSchema()); p.put("products", Map.of("type", "array", "minItems", 1, "maxItems", 50, "items", productDraftSchema())); p.put("productIds", Map.of("type", "array", "minItems", 1, "maxItems", 50, "items", integerSchema())); p.put("field", Map.of("type", "string")); p.put("value", Map.of()); p.put("batchOperation", Map.of("type", "string")); p.put("previewId", Map.of("type", "string")); p.put("confirmation", Map.of("type", "boolean")); p.put("requestId", Map.of("type", "string")); return p; }
    private static Map<String, Object> productDraftSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("productName", Map.of("type", "string"));
        properties.put("productSn", Map.of("type", "string"));
        properties.put("productPrice", Map.of("type", "number", "minimum", 0));
        properties.put("categoryId", integerSchema());
        properties.put("categoryName", Map.of("type", "string"));
        properties.put("productStock", Map.of("type", "integer"));
        properties.put("productStatus", Map.of("type", "integer"));
        properties.put("productBrief", Map.of("type", "string"));
        properties.put("productDesc", Map.of("type", "string"));
        properties.put("images", stringArraySchema());
        properties.put("detailImages", stringArraySchema());
        properties.put("media", Map.of("type", "array", "items", productMediaSchema()));
        properties.put("marketPrice", Map.of("type", "number", "minimum", 0));
        properties.put("costPrice", Map.of("type", "number", "minimum", 0));
        properties.put("productTsn", Map.of("type", "string"));
        properties.put("keywords", Map.of("type", "string"));
        properties.put("skus", Map.of("type", "array", "items", productSkuSchema()));
        properties.put("fieldSources", Map.of("type", "object", "additionalProperties", false,
                "patternProperties", Map.of("^.+$", Map.of("type", "string", "enum", List.of("user_provided", "ai_recommended", "site_default")))));
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }
    private static Map<String, Object> stringArraySchema() { return Map.of("type", "array", "items", Map.of("type", "string")); }
    private static Map<String, Object> productMediaSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("assetId", integerSchema()); properties.put("type", Map.of("type", "string"));
        properties.put("role", Map.of("type", "string")); properties.put("url", Map.of("type", "string"));
        properties.put("coverUrl", Map.of("type", "string")); properties.put("format", Map.of("type", "string"));
        return strictObjectSchema(properties);
    }
    private static Map<String, Object> productSkuSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        Map<String, Object> specs = new LinkedHashMap<>(); specs.put("type", "object"); specs.put("patternProperties", Map.of("^.+$", Map.of("type", "string"))); specs.put("additionalProperties", false);
        properties.put("specs", specs); properties.put("price", Map.of("type", "number", "minimum", 0));
        properties.put("stock", Map.of("type", "integer")); properties.put("skuSn", Map.of("type", "string"));
        properties.put("skuTsn", Map.of("type", "string")); properties.put("marketPrice", Map.of("type", "number", "minimum", 0));
        properties.put("costPrice", Map.of("type", "number", "minimum", 0));
        return strictObjectSchema(properties);
    }
    private static Map<String, Object> strictObjectSchema(Map<String, Object> properties) {
        Map<String, Object> schema = new LinkedHashMap<>(); schema.put("type", "object"); schema.put("properties", properties); schema.put("additionalProperties", false); return schema;
    }
    private static McpSchema.Tool tool(String name, String description, Map<String, Object> properties, String... required) { Map<String, Object> schema = new LinkedHashMap<>(); schema.put("type", "object"); schema.put("properties", properties); if (required.length > 0) schema.put("required", List.of(required)); schema.put("additionalProperties", false); return McpSchema.Tool.builder().name(name).description(description).inputSchema(schema).build(); }
}
