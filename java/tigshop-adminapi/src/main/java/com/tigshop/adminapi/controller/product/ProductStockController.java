package com.tigshop.adminapi.controller.product;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.tigshop.bean.dto.product.*;
import com.tigshop.bean.vo.product.*;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.ExcelUtils;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SkuValueTranslateUtils;
import com.tigshop.mapper.o2o.StoreProductMapper;
import com.tigshop.service.ProductStockAdjustRecordService;
import com.tigshop.service.product.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/adminapi/product/stock")
@Tag(name = "商品库存管理")
public class ProductStockController {

    @Autowired
    private ProductSkuService productSkuService;

    @Autowired
    private ProductStockRecordService productStockRecordService;

    @Autowired
    private ProductStockAdjustRecordService productStockAdjustRecordService;

    @Autowired
    private ProductStockAllocateRecordService productStockAllocateRecordService;

    @Autowired
    private ProductInventoryLogService productInventoryLogService;

    @Autowired
    private ProductStockAllocateOperationRecordService productStockAllocateOperationRecordService;
    @Autowired
    private StoreProductMapper storeProductMapper;

    @Autowired
    private TranslatePackage translatePackage;

    @Operation(summary = "获取库存列表")
    @GetMapping("/page")
    public Page<ProductStockPageVO> page(ProductStockDTO productStockDTO) {
        int shopId = HeaderUtils.getShopId() == null ? 0 : HeaderUtils.getShopId();
        if (productStockDTO.getShopId() == null) {
            productStockDTO.setShopId(shopId);
        }
        return productSkuService.getProductStockRecordPage(productStockDTO);
    }

    @Operation(summary = "获取出入库明细")
    @GetMapping("/stockDetail")
    public Page<ProductStockDetailVO> stockDetail(ProductStockDetailDTO productStockDetailDTO) {
        return productInventoryLogService.getDetail(productStockDetailDTO);
    }

    @Operation(summary = "获取出入库列表")
    @GetMapping("/record/page")
    public Page<ProductStockRecordVO> productStockRecordPage(ProductStockDTO productStockDTO) {
        return productStockRecordService.productStockRecordPage(productStockDTO);
    }

    @Operation(summary = "获取出入库详情")
    @GetMapping("/getRecordDetail")
    public ProductStockRecordVO getRecordDetail(@RequestParam("id") Integer id) {
        return productStockRecordService.getRecordDetail(id);
    }

    @PreAuthorize("@pms.hasPermission('productStockInUpdateManage')")
    @Operation(summary = "商品入库")
    @PostMapping("/stockIn")
    public boolean productStockIn(@RequestBody ProductStockInDTO productStockInDTO) {
        return productStockRecordService.productStockIn(productStockInDTO);
    }

    @Operation(summary = "商品出库")
    @PostMapping("/stockOut")
    @PreAuthorize("@pms.hasPermission('productStockOutUpdateManage')")
    public boolean productStockOut(@RequestBody ProductStockInDTO productStockInDTO) {
        return productStockRecordService.productStockOut(productStockInDTO);
    }

    @GetMapping("/downloadTemplate")
    @Operation(summary = "下载商品导入模版", description = "下载商品导入模版")
    public void downloadTemplate(HttpServletResponse response) {
        productStockRecordService.downloadTemplate(response);
    }

    @Operation(summary = "批量导入商品")
    @PostMapping("/batchImport")
    public List<ProductImportSuccessListVO> batchImport(@RequestParam("file") MultipartFile file) {
        return productStockRecordService.batchImport(file);
    }

    @Operation(summary = "获取导入失败的数据")
    @PostMapping("/getImportErrorData")
    public void getImportErrorData(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        productStockRecordService.getImportErrorData(file, response);
    }

    @Operation(summary = "商品库存调整")
    @PostMapping("/stockAdjust")
    @PreAuthorize("@pms.hasPermission('productStockAdjustUpdateManage')")
    public boolean stockAdjust(@RequestBody ProductStockAdjustDTO productStockAdjustDTO) {
        return productStockAdjustRecordService.stockAdjust(productStockAdjustDTO);
    }

    @Operation(summary = "商品调整记录")
    @GetMapping("/adjustDetail")
    public List<ProductStockAdjustRecordDetailVO> adjustDetail(@RequestParam("productId") Integer productId, @RequestParam(name = "skuId", required = false) Integer skuId) {
        return productStockAdjustRecordService.adjustDetail(productId, skuId);
    }

    @Operation(summary = "获取可调拨商品列表")
    @GetMapping("/getAllocateProductStockPage")
    public Page<ProductStockPageVO> getAllocateProductStockPage(ProductStockDTO productStockDTO) {

        Assert.isTrue(productStockDTO.getShopId() != null, () -> new GlobalException("请选择出库组织"));
        Assert.isTrue(productStockDTO.getTargetShopId() != null, () -> new GlobalException("请选择入库组织"));

        Page<ProductStockPageVO> page = new Page<>(productStockDTO.getPage(), productStockDTO.getSize());

        // 场景1：平台调拨给门店 - shopId=0, targetShopId>0
        // 查询平台商品中已分配给目标门店的商品
        if (productStockDTO.getShopId() == 0 && productStockDTO.getTargetShopId() > 0) {
            return translateAllocateStockPage(storeProductMapper.getPlatformAllocateToStorePage(page, productStockDTO));
        }

        // 场景2：门店调拨给平台 - shopId>0, targetShopId=0
        // 查询门店已分配的商品
        if (productStockDTO.getShopId() > 0 && productStockDTO.getTargetShopId() == 0) {
            return translateAllocateStockPage(storeProductMapper.getStoreAllocateToPlatformPage(page, productStockDTO));
        }

        // 场景3：门店调拨给门店 - shopId>0, targetShopId>0
        // 查询两个门店都有分配的商品（product_id相同）
        if (productStockDTO.getShopId() > 0 && productStockDTO.getTargetShopId() > 0) {
            return translateAllocateStockPage(storeProductMapper.getStoreAllocateToStorePage(page, productStockDTO));
        }

        // 默认返回空
        return page;
    }

    /**
     * 调拨选品列表：Mapper 直出，补商品名/规格/缩略图翻译。
     */
    private Page<ProductStockPageVO> translateAllocateStockPage(Page<ProductStockPageVO> result) {
        if (result == null || result.getRecords() == null) {
            return result;
        }
        for (ProductStockPageVO vo : result.getRecords()) {
            if (vo == null) {
                continue;
            }
            if (StrUtil.isNotBlank(vo.getProductName())) {
                vo.setProductName(translatePackage.translate(vo.getProductName(), DataType.PRODUCT.getCode()));
            }
            if (StrUtil.isNotBlank(vo.getProductSkuName())) {
                vo.setProductSkuName(SkuValueTranslateUtils.translateSkuValue(vo.getProductSkuName(), translatePackage));
            }
            if (StrUtil.isNotBlank(vo.getProductPicThumb())) {
                vo.setProductPicThumb(translatePackage.translate(vo.getProductPicThumb(), DataType.IMAGE_TITLE.getCode()));
            }
        }
        return result;
    }


    @Operation(summary = "库存调拨记录")
    @GetMapping("/productStockAllocateRecordPage")
    public Page<ProductStockAllocateRecordPageVO> productStockAllocateRecordPage(ProductStockAllocateRecordDTO productStockAllocateRecordDTO) {
        return productStockAllocateRecordService.getProductStockAllocateRecordPage(productStockAllocateRecordDTO);
    }

    @Operation(summary = "库存调拨记录详情")
    @GetMapping("/productStockAllocateRecordDetail")
    public ProductStockAllocateRecordDetailVO productStockAllocateRecordDetail(@RequestParam("id") Integer id) {
        return productStockAllocateRecordService.productStockAllocateRecordDetail(id);
    }


    @Operation(summary = "新建调拨单")
    @PostMapping("/createAllocate")
    @PreAuthorize("@pms.hasPermission('productStockAllocateUpdateManage')")
    public boolean createAllocate(@RequestBody ProductStockAllocateRecordCreateDTO createDTO) {
        return productStockAllocateRecordService.createAllocate(createDTO);
    }

    @Operation(summary = "组织集合")
    @GetMapping("/getOrgList")
    public List<AllocateOrgVO> getOrgList() {
        return productStockAllocateRecordService.getOrgList();
    }

    @Operation(summary = "获取经办人集合")
    @GetMapping("/getAddByList")
    public List<String> getAddBy() {
        return productStockAllocateRecordService.getAddBy();
    }

    @Operation(summary = "调拨审核")
    @PostMapping("/auditAllocate")
    @PreAuthorize("@pms.hasPermission('productStockAllocateUpdateManage')")
    public boolean auditAllocate(@RequestParam("id") Integer id, @RequestParam("auditType") Integer auditType, @RequestParam(name = "remark", required = false) String remark) {
        return productStockAllocateRecordService.auditAllocate(id, auditType, remark);
    }

    @Operation(summary = "调拨作废")
    @PostMapping("/cancelAllocate")
    @PreAuthorize("@pms.hasPermission('productStockAllocateUpdateManage')")
    public boolean cancelAllocate(@RequestBody List<Integer> allocateIds) {
        return productStockAllocateRecordService.cancelAllocate(allocateIds);
    }

    @Operation(summary = "调拨出库")
    @PostMapping("/stockOutAllocate")
    @PreAuthorize("@pms.hasPermission('productStockAllocateUpdateManage')")
    public boolean stockOutAllocate(@RequestBody AllocateStockOutDTO allocateStockOutDTO) {
        return productStockAllocateRecordService.stockOutAllocate(allocateStockOutDTO);
    }

    @Operation(summary = "调拨入库")
    @PostMapping("/stockInAllocate")
    @PreAuthorize("@pms.hasPermission('productStockAllocateUpdateManage')")
    public boolean stockInAllocate(@RequestBody AllocateStockOutDTO allocateStockOutDTO) {
        return productStockAllocateRecordService.stockInAllocate(allocateStockOutDTO);
    }

    @ResponseExcel
    @Operation(summary = "调拨单导出")
    @PostMapping("/exportAllocate")
    public List<ProductStockAllocateExcelVO> exportAllocate(@RequestBody ProductStockAllocateExcelExportDTO excelExportDTO) {
       return productStockAllocateRecordService.exportAllocate(excelExportDTO);
    }


    @Operation(summary = "下载导入调拨单Excel")
    @GetMapping("/getAllocateTemplate")
    public void getAllocateTemplate(HttpServletResponse response) {
        ExcelUtils<AllocateBatchImportVO> excelUtils = new ExcelUtils<>(List.of(), AllocateBatchImportVO.class);
        excelUtils.exportExcelWeb(response);
    }


    @Operation(summary = "批量导入调拨商品")
    @PostMapping("/batchImportAllocate")
    public boolean batchImportAllocate(@RequestParam("file") MultipartFile file) {
        return productStockAllocateRecordService.batchImportAllocate(file);
    }

    @Operation(summary = "获取导入调拨商品失败Excel")
    @PostMapping("/getBatchImportErrorData")
    public void getBatchImportErrorData(@RequestParam("file") MultipartFile file,HttpServletResponse response) {
        productStockAllocateRecordService.getBatchImportErrorData(file,response);
    }


    @Operation(summary = "库存调拨操作记录详情")
    @GetMapping("/productStockAllocateOperationDetail")
    public List<ProductStockAllocateOperationRecordVO> productStockAllocateOperationDetail(@RequestParam("allocateId") Integer allocateId) {
        return productStockAllocateOperationRecordService.productStockAllocateOperationDetail(allocateId);
    }

    @GetMapping("/downloadAllocateTemplate")
    @Operation(summary = "下载调拨商品导入模版", description = "下载调拨商品导入模版")
    public void downloadAllocateTemplate(HttpServletResponse response) {
        ExcelUtils<AllocateProductImportExcelDTO> excelUtils = new ExcelUtils<>(List.of(), AllocateProductImportExcelDTO.class);
        excelUtils.exportExcelWeb(response);
    }

    @Operation(summary = "批量导入调拨商品")
    @PostMapping("/batchImportAllocateProduct")
    public List<ProductImportSuccessListVO> batchImportAllocateProduct(@RequestParam("file") MultipartFile file,
                                                                        @RequestParam(name = "shopId") Integer shopId,
                                                                        @RequestParam(name = "targetShopId") Integer targetShopId) {
        return productStockRecordService.batchImportAllocateProduct(file, shopId, targetShopId);
    }

    @Operation(summary = "获取调拨商品导入失败的数据")
    @PostMapping("/getAllocateProductErrorData")
    public void getAllocateProductErrorData(@RequestParam("file") MultipartFile file,
                                            @RequestParam(name = "shopId") Integer shopId,
                                            @RequestParam(name = "targetShopId") Integer targetShopId,
                                            HttpServletResponse response) {
        productStockRecordService.getAllocateProductErrorData(file, shopId, targetShopId, response);
    }

}
