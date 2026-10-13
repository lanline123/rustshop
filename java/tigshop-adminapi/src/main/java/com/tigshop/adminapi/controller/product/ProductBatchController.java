package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.tigshop.bean.dto.product.ProductBatchDealDTO;
import com.tigshop.bean.dto.product.ProductBatchEditDTO;
import com.tigshop.bean.dto.product.ServicesDTO;
import com.tigshop.bean.vo.product.ProductBatchModifyVO;
import com.tigshop.bean.vo.product.ProductImportExcelVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.product.ProductBatchService;
import com.tigshop.service.product.ServicesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 产品批量操作 控制器
 *
 * @author kidd
 * @since 2025/3/26 16:10
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/adminapi/product/productBatch")
@Tag(name = "商品")
@PreAuthorize("@pms.hasPermission('productBatchAddManage')")
public class ProductBatchController {

    private final ProductBatchService productBatchService;

    @Resource
    private ServicesService servicesService;

    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载商品导入模版", description = "下载商品导入模版（二进制 xlsx 流，非 JSON）")
    public void downloadTemplate(HttpServletResponse response) {
        productBatchService.downloadTemplate(response);
    }

    @ResponseExcel
    @GetMapping("/productBatchDeal")
    @Operation(summary = "商品导出Excel", description = "商品导出Excel")
    public List<ProductImportExcelVO> productBatchDeal(ProductBatchDealDTO dto) {
        return productBatchService.productBatchDeal(dto);
    }

    @PostMapping("/productBatchModify")
    @Operation(summary = "商品导入", description = "商品导入")
    @PreAuthorize("@pms.hasPermission('productBatchModifyManage')")
    public ProductBatchModifyVO productBatchModify(
            @RequestParam("isAutoCat") Integer isAutoCat,
            @RequestParam("isAutoBrand") Integer isAutoBrand,
            @RequestParam("isChangePic") Integer isChangePic,
            @RequestParam("isUpload") Integer isUpload,
            @RequestParam("file") MultipartFile file) {
        return productBatchService.productBatchModify(isAutoCat, isAutoBrand, isChangePic, isUpload, file);
    }

    @PostMapping("/productBatchEdit")
    @Operation(summary = "商品批量修改", description = "商品批量修改")
    @PreAuthorize("@pms.hasPermission('productBatchEditModifyManage')")
    public void productBatchEdit(@RequestBody ProductBatchEditDTO dto) {
        productBatchService.productBatchEdit(dto);
    }

    @GetMapping("/productService/list")
    @Operation(summary = "商品服务列表")
    public Page<ServicesDTO> list(BasePage basePage) {
        return servicesService.list(basePage);
    }
}
