package com.tigshop.adminapi.controller.openapi.workbuddy;

import com.tigshop.bean.dto.workbuddy.ProductDraftDTO;
import com.tigshop.bean.dto.workbuddy.MediaUploadRequestDTO;
import com.tigshop.bean.vo.workbuddy.MediaUploadResultVO;
import com.tigshop.bean.vo.workbuddy.*;
import com.tigshop.adminapi.mcp.WorkBuddyAssistantGuide;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.workbuddy.*;
import com.tigshop.helper.SettingsHelper;
import com.tigshop.bean.enums.setting.SettingsEnum;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * WorkBuddy MCP 开放接口。
 *
 * <p>路径必须保留 {@code /adminapi} 段：网关只把该前缀转发到本服务，
 * 其余路径会落到前台站点。该前缀在 SecurityConfig 中放行 JWT/API Token，鉴权由本类的 {@link #auth} 完成。
 * 响应不经 AjaxResult 包装，异常由 {@link WorkBuddyApiExceptionHandler} 转为真实 HTTP 状态码，
 * 以便 MCP 连接器按状态码判定失败（CR-20260827-164700 FIX-02 方案 A）。
 */
@IgnoreResponseAdvice
@RestController
@RequestMapping("/adminapi/openapi/mcp/v1")
public class WorkBuddyProductImportController {

    private static final String KEY_HEADER = "X-MCP-Api-Key";
    private static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    @Resource
    private WorkBuddyKeyService ks;

    @Resource
    private ProductImportService is;

    @Resource
    private ProductContextService productContextService;

    @Resource
    private McpMediaUploadService mcpMediaUploadService;

    @Value("${spring.servlet.multipart.max-file-size:50MB}")
    private String multipartMaxFileSize;

    @Resource
    private SettingsHelper settingsHelper;

    @GetMapping("/connection/status")
    public WorkBuddyKeyStatusVO status(HttpServletRequest r) {
        return ks.status(r.getHeader(KEY_HEADER), null);
    }

    @GetMapping("/import/requirements")
    public ProductRequirementsVO requirements(HttpServletRequest r) {
        auth(r, "product:read");
        return execute(r, () -> is.requirements());
    }

    @GetMapping("/categories")
    public List<ProductCategoryVO> categories(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) Integer parentId,
                                              HttpServletRequest r) {
        auth(r, "product:read");
        return execute(r, () -> is.categories(keyword, parentId));
    }

    @PostMapping("/products/validate")
    public ProductValidationVO validate(@Valid @RequestBody ProductList b, HttpServletRequest r) {
        Integer s = auth(r, "product:write");
        return execute(r, () -> is.validate(b.getProducts(), s));
    }

    @PostMapping("/products/import")
    public ProductImportResultVO imp(@RequestBody ImportBody b, HttpServletRequest r) {
        Integer s = auth(r, "product:write");
        return execute(r, () -> is.importProducts(b.getProducts(), s, b.getPreviewId(),
                r.getHeader(IDEMPOTENCY_HEADER), b.isConfirmation()));
    }

    @PostMapping("/media/upload")
    public MediaUploadResultVO mediaUpload(@RequestBody MediaUploadRequestDTO body, HttpServletRequest r) {
        Integer shopId = auth(r, "product:write");
        if (body == null || body.getFiles() == null || body.getFiles().stream().anyMatch(f -> f == null || f.getFileToken() == null || f.getFileToken().isBlank())) {
            throw new GlobalException("媒体上传只允许 fileToken；请先通过 multipart media/upload-token 获取短时令牌，禁止使用 Base64、本地路径或其他上传方式", HttpStatus.BAD_REQUEST);
        }
        return execute(r, () -> mcpMediaUploadService.uploadByTokens(body, shopId));
    }

    /** 接收附件并生成短时 fileToken，避免 MCP 在对话中传输 Base64。 */
    @PostMapping(value = "/media/upload-token", consumes = "multipart/form-data")
    public Map<String, Object> mediaUploadToken(@RequestPart("files") List<MultipartFile> files, HttpServletRequest r) {
        auth(r, "product:write");
        return execute(r, () -> mcpMediaUploadService.stage(files));
    }

    @GetMapping("/capabilities")
    public McpCapabilitiesVO capabilities(HttpServletRequest r) {
        auth(r, "product:read");
        return execute(r, () -> {
            McpCapabilitiesVO v = new McpCapabilitiesVO();
            try {
                String raw = multipartMaxFileSize == null ? "" : multipartMaxFileSize.trim().toUpperCase();
                long bytes = raw.endsWith("GB") ? Long.parseLong(raw.replace("GB", "").trim()) * 1073741824L : raw.endsWith("KB") ? Long.parseLong(raw.replace("KB", "").trim()) * 1024L : raw.endsWith("MB") ? Long.parseLong(raw.replace("MB", "").trim()) * 1048576L : Long.parseLong(raw);
                if (bytes > 0) v.getMedia().setMaxFileBytes(bytes);
            } catch (Exception ignored) { }
            v.getMedia().setImageMaxFileBytes(v.getMedia().getMaxFileBytes());
            try {
                java.math.BigDecimal imageMb = settingsHelper == null ? null : settingsHelper.get(SettingsEnum.IMAGE_UPLOAD_MAX_SIZE);
                if (imageMb != null && imageMb.signum() > 0) {
                    long imageBytes = imageMb.multiply(java.math.BigDecimal.valueOf(1048576L)).longValue();
                    v.getMedia().setImageMaxFileBytes(Math.min(v.getMedia().getMaxFileBytes(), imageBytes));
                }
            } catch (Exception ignored) { }
            if (v.getMedia().getImageMaxFileBytes() == null) v.getMedia().setImageMaxFileBytes(v.getMedia().getMaxFileBytes());
            v.getMedia().setVideoMaxFileBytes(v.getMedia().getMaxFileBytes());
            // REST capabilities 与 MCP initialize 必须复用同一份全局指引，避免不同客户端拿到不同规则。
            v.setAssistant(WorkBuddyAssistantGuide.capabilities());
            return v;
        });
    }

    @GetMapping("/products/context")
    public ProductContextVO context(@RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Integer categoryId,
                                    @RequestParam(required = false) Integer sampleSize,
                                    HttpServletRequest r) {
        Integer shopId = auth(r, "product:read");
        return execute(r, () -> productContextService.context(keyword, categoryId, sampleSize, shopId));
    }

    private <T> T execute(HttpServletRequest request, Supplier<T> action) {
        return com.tigshop.adminapi.mcp.WorkBuddyRequestContext.runAsPlatformAdmin(request, action);
    }

    private Integer auth(HttpServletRequest r, String scope) {
        String key = r.getHeader(KEY_HEADER);
        WorkBuddyKeyStatusVO s = ks.status(key, null);
        if (s == null || !s.isValid() || s.getShopId() == null) {
            String message = s == null || s.getMessage() == null ? "MCP密钥无效" : s.getMessage();
            throw new GlobalException(message, HttpStatus.UNAUTHORIZED);
        }
        ks.requireValid(key, s.getShopId(), scope);
        return s.getShopId();
    }

    @Data
    public static class ProductList {
        private List<ProductDraftDTO> products;
    }

    @Data
    public static class ImportBody extends ProductList {
        private String previewId;

        /** 与 MCP 工具 schema 的 confirmation 字段对齐（CR-20260827-164700 FIX-01）。 */
        private boolean confirmation;
    }
}
