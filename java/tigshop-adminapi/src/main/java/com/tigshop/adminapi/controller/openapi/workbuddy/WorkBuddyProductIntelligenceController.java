package com.tigshop.adminapi.controller.openapi.workbuddy;

import com.tigshop.bean.dto.workbuddy.CategoryImportDTO;
import com.tigshop.bean.dto.workbuddy.MediaOperationDTO;
import com.tigshop.bean.dto.workbuddy.MediaQueryDTO;
import com.tigshop.bean.dto.workbuddy.ProductAnalysisDTO;
import com.tigshop.bean.vo.workbuddy.WorkBuddyKeyStatusVO;
import com.tigshop.adminapi.mcp.WorkBuddyRequestContext;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.workbuddy.CategoryImportService;
import com.tigshop.service.workbuddy.MediaManagementService;
import com.tigshop.service.workbuddy.ProductAnalysisService;
import com.tigshop.service.workbuddy.WorkBuddyKeyService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.function.Supplier;

/**
 * MCP 商品智能化扩展的 REST 兼容入口。
 *
 * <p>Node/HTTP 连接器和原生 MCP 端点共用同一套 service，避免只在 MCP transport
 * 中注册工具而导致通用 HTTP 客户端无法使用素材、分类和分析能力。
 */
@IgnoreResponseAdvice
@RestController
@RequestMapping("/adminapi/openapi/mcp/v1")
public class WorkBuddyProductIntelligenceController {
    private static final String KEY_HEADER = "X-MCP-Api-Key";

    @Resource
    private WorkBuddyKeyService keyService;
    @Resource
    private MediaManagementService mediaManagementService;
    @Resource
    private CategoryImportService categoryImportService;
    @Resource
    private ProductAnalysisService productAnalysisService;

    @GetMapping("/media")
    public Map<String, Object> media(MediaQueryDTO query, HttpServletRequest request) {
        MediaQueryDTO safeQuery = query == null ? new MediaQueryDTO() : query;
        return execute(request, () -> mediaManagementService.query(safeQuery, auth(request, "media:read")));
    }

    @PostMapping("/media/prepare")
    public Map<String, Object> mediaPrepare(@RequestBody(required = false) MediaOperationDTO operation,
                                            HttpServletRequest request) {
        MediaOperationDTO safeOperation = operation == null ? new MediaOperationDTO() : operation;
        return execute(request, () -> mediaManagementService.prepare(safeOperation, auth(request, "media:write")));
    }

    @PostMapping("/media/execute")
    public Map<String, Object> mediaExecute(@RequestBody(required = false) MediaOperationDTO operation,
                                            HttpServletRequest request) {
        MediaOperationDTO safeOperation = operation == null ? new MediaOperationDTO() : operation;
        return execute(request, () -> mediaManagementService.execute(safeOperation, auth(request, "media:write")));
    }

    @PostMapping("/categories/prepare")
    public Map<String, Object> categoryPrepare(@RequestBody(required = false) CategoryImportDTO operation,
                                               HttpServletRequest request) {
        CategoryImportDTO safeOperation = operation == null ? new CategoryImportDTO() : operation;
        return execute(request, () -> categoryImportService.prepare(safeOperation, auth(request, "category:write")));
    }

    @PostMapping("/categories/execute")
    public Map<String, Object> categoryExecute(@RequestBody(required = false) CategoryImportDTO operation,
                                               HttpServletRequest request) {
        CategoryImportDTO safeOperation = operation == null ? new CategoryImportDTO() : operation;
        return execute(request, () -> categoryImportService.execute(safeOperation, auth(request, "category:write")));
    }

    @GetMapping("/products/analysis")
    public com.tigshop.bean.vo.workbuddy.ProductAnalysisVO productAnalysis(ProductAnalysisDTO query,
                                                                            HttpServletRequest request) {
        ProductAnalysisDTO safeQuery = query == null ? new ProductAnalysisDTO() : query;
        return execute(request, () -> productAnalysisService.analyze(safeQuery, auth(request, "analysis:read")));
    }

    private <T> T execute(HttpServletRequest request, Supplier<T> action) {
        return WorkBuddyRequestContext.runAsPlatformAdmin(request, action);
    }

    private Integer auth(HttpServletRequest request, String scope) {
        String key = request.getHeader(KEY_HEADER);
        WorkBuddyKeyStatusVO status = keyService.status(key, null);
        if (status == null || !status.isValid() || status.getShopId() == null) {
            throw new GlobalException(status == null ? "MCP密钥无效" : status.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        keyService.requireValid(key, status.getShopId(), scope);
        return status.getShopId();
    }
}
