package com.tigshop.adminapi.controller.openapi.workbuddy;

import com.tigshop.bean.dto.workbuddy.ProductSearchDTO;
import com.tigshop.bean.vo.workbuddy.*;
import com.tigshop.common.annotation.IgnoreResponseAdvice;
import com.tigshop.common.constant.HttpStatus;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.workbuddy.ProductManageService;
import com.tigshop.service.workbuddy.WorkBuddyKeyService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.function.Supplier;

@IgnoreResponseAdvice
@RestController
@RequestMapping("/adminapi/openapi/mcp/v1/products")
public class WorkBuddyProductManageController {
    private static final String KEY_HEADER = "X-MCP-Api-Key";
    @Resource private ProductManageService productManageService;
    @Resource private WorkBuddyKeyService keyService;

    @GetMapping
    public ProductSearchVO search(ProductSearchDTO query, HttpServletRequest request) { return execute(request, () -> productManageService.search(query, auth(request, "product:read"))); }
    @GetMapping("/{id}")
    public ProductDetailVO detail(@PathVariable Integer id, HttpServletRequest request) { return execute(request, () -> productManageService.detail(id, auth(request, "product:read"))); }
    @GetMapping("/{id}/promotion-links")
    public PromotionLinksVO promotion(@PathVariable Integer id, HttpServletRequest request) { return execute(request, () -> productManageService.promotionLinks(id, auth(request, "product:read"))); }
    @PostMapping("/{id}/field")
    public ProductWriteResultVO field(@PathVariable Integer id, @RequestBody FieldBody body, HttpServletRequest request) {
        requireConfirmation(body);
        return execute(request, () -> productManageService.updateField(id, body.getField(), body.getValue(), auth(request, "product:write")));
    }
    @PostMapping("/{id}/status")
    public ProductWriteResultVO status(@PathVariable Integer id, @RequestBody StatusBody body, HttpServletRequest request) {
        requireConfirmation(body);
        return execute(request, () -> productManageService.setStatus(id, body.isOnSale(), auth(request, "product:write")));
    }
    @PostMapping("/{id}/recycle")
    public ProductWriteResultVO recycle(@PathVariable Integer id, @RequestBody(required = false) ConfirmBody body, HttpServletRequest request) {
        requireConfirmation(body);
        return execute(request, () -> productManageService.recycle(id, auth(request, "product:write")));
    }
    @PostMapping("/{id}/restore")
    public ProductWriteResultVO restore(@PathVariable Integer id, @RequestBody(required = false) ConfirmBody body, HttpServletRequest request) {
        requireConfirmation(body);
        return execute(request, () -> productManageService.restore(id, auth(request, "product:write")));
    }
    @PostMapping("/{id}/copy")
    public ProductWriteResultVO copy(@PathVariable Integer id, @RequestBody(required = false) ConfirmBody body, HttpServletRequest request) {
        requireConfirmation(body);
        return execute(request, () -> productManageService.copy(id, auth(request, "product:write")));
    }
    @PostMapping("/{id}/delete")
    public ProductWriteResultVO delete(@PathVariable Integer id, @RequestBody(required = false) ConfirmBody body, HttpServletRequest request) {
        requireConfirmation(body);
        return execute(request, () -> productManageService.deletePermanently(id, auth(request, "product:write")));
    }
    @PostMapping("/batch/preview")
    public BatchPreviewVO preview(@RequestBody @Valid BatchBody body, HttpServletRequest request) { return execute(request, () -> productManageService.previewBatch(body.getOperation(), body.getProductIds(), auth(request, "product:write"))); }
    @PostMapping("/batch/execute")
    public BatchResultVO execute(@RequestBody @Valid BatchExecuteBody body, HttpServletRequest request) { return execute(request, () -> productManageService.executeBatch(body.getOperation(), body.getProductIds(), body.getPreviewId(), body.isConfirmation(), auth(request, "product:write"))); }

    private <T> T execute(HttpServletRequest request, Supplier<T> action) {
        return com.tigshop.adminapi.mcp.WorkBuddyRequestContext.runAsPlatformAdmin(request, action);
    }

    private Integer auth(HttpServletRequest request, String scope) {
        String key = request.getHeader(KEY_HEADER);
        WorkBuddyKeyStatusVO status = keyService.status(key, null);
        if (status == null || !status.isValid() || status.getShopId() == null) {
            String message = status == null || status.getMessage() == null ? "MCP密钥无效" : status.getMessage();
            throw new GlobalException(message, HttpStatus.UNAUTHORIZED);
        }
        keyService.requireValid(key, status.getShopId(), scope);
        return status.getShopId();
    }
    private static void requireConfirmation(ConfirmBody body) {
        if (body == null || !body.isConfirmation()) {
            throw new GlobalException("写入前必须明确 confirmation: true", HttpStatus.BAD_REQUEST);
        }
    }

    @Data public static class FieldBody extends ConfirmBody { private String field; private Object value; }
    @Data public static class StatusBody extends ConfirmBody { private boolean onSale; }
    @Data public static class ConfirmBody { private boolean confirmation; }
    @Data public static class BatchBody { @NotEmpty private List<Integer> productIds; @NotNull private String operation; }
    @Data public static class BatchExecuteBody extends BatchBody { @NotNull private String previewId; private boolean confirmation; }
}
