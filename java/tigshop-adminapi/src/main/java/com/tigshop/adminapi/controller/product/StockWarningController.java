package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.tigshop.bean.dto.product.StockWarningBatchSaveDTO;
import com.tigshop.bean.dto.product.StockWarningExportDTO;
import com.tigshop.bean.dto.product.StockWarningListDTO;
import com.tigshop.bean.dto.product.StockWarningSaveDTO;
import com.tigshop.bean.dto.product.StockWarningSettingSaveDTO;
import com.tigshop.bean.enums.adminuser.AdminTypeEnum;
import com.tigshop.bean.vo.product.StockWarningBatchResultVO;
import com.tigshop.bean.vo.product.StockWarningExcelVO;
import com.tigshop.bean.vo.product.StockWarningImportResultVO;
import com.tigshop.bean.vo.product.StockWarningPageVO;
import com.tigshop.bean.vo.product.StockWarningSettingVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.service.product.StockWarningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 库存预警控制器
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/product/stockWarning")
@Tag(name = "库存预警", description = "库存预警开关与商品预警规则")
@Validated
@PreAuthorize("@pms.hasPermission('stockWarningManage')")
public class StockWarningController {

    @Resource
    private StockWarningService stockWarningService;


    @Operation(summary = "获取库存预警通知总开关")
    @GetMapping("/setting")
    public StockWarningSettingVO setting(@RequestParam(value = "shopId", required = false) Integer shopId) {
        return stockWarningService.getSetting(resolveShopId(shopId));
    }

    @PreAuthorize("@pms.hasPermission('stockWarningUpdateManage')")
    @Operation(summary = "保存库存预警通知总开关")
    @PostMapping("/setting")
    public void saveSetting(@Valid @RequestBody StockWarningSettingSaveDTO saveDTO) {
        saveDTO.setShopId(resolveShopId(saveDTO.getShopId()));
        stockWarningService.saveSetting(saveDTO);
    }

    @Operation(summary = "获取库存预警列表")
    @GetMapping("/list")
    public Page<StockWarningPageVO> list(StockWarningListDTO listDTO) {
        listDTO.setShopId(resolveShopId(listDTO.getShopId()));
        return stockWarningService.list(listDTO);
    }

    @ResponseExcel
    @Operation(summary = "导出库存预警列表", description = "按前端勾选的行导出，ids 为必填的 productId_skuId 列表")
    @GetMapping("/export")
    public List<StockWarningExcelVO> export(@Valid StockWarningExportDTO exportDTO) {
        exportDTO.setShopId(resolveShopId(exportDTO.getShopId()));
        return stockWarningService.export(exportDTO);
    }

    @Operation(summary = "下载库存预警导入模板", description = "二进制 xlsx 流，非 JSON；列为 商品编码/SKU编码/下限/上限")
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) {
        stockWarningService.downloadTemplate(response);
    }

    @PreAuthorize("@pms.hasPermission('stockWarningUpdateManage')")
    @Operation(summary = "导入库存预警配置",
            description = "商品编码和SKU编码至少填写一个；商品编码填写时校验，多规格必须填写SKU编码；供应商按SKU编码导入；导入的规则一律置为开启")
    @PostMapping("/import")
    public StockWarningImportResultVO importExcel(@RequestParam("file") MultipartFile file,
                                                 @RequestParam(value = "shopId", required = false) Integer shopId) {
        return stockWarningService.importExcel(file, resolveShopId(shopId));
    }

    @PreAuthorize("@pms.hasPermission('stockWarningUpdateManage')")
    @Operation(summary = "获取库存预警导入失败数据")
    @PostMapping({"/getImportErrorData", "/getBatchImportErrorData"})
    public void getImportErrorData(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "shopId", required = false) Integer shopId,
                                   HttpServletResponse response) {
        stockWarningService.getImportErrorData(file, resolveShopId(shopId), response);
    }

    @PreAuthorize("@pms.hasPermission('stockWarningUpdateManage')")
    @Operation(summary = "设置商品库存预警")
    @PostMapping("/save")
    public void save(@Valid @RequestBody StockWarningSaveDTO saveDTO) {
        saveDTO.setShopId(resolveShopId(saveDTO.getShopId()));
        stockWarningService.save(saveDTO);
    }

    @PreAuthorize("@pms.hasPermission('stockWarningUpdateManage')")
    @Operation(summary = "批量设置商品库存预警")
    @PostMapping("/batchSave")
    public StockWarningBatchResultVO batchSave(@Valid @RequestBody StockWarningBatchSaveDTO batchSaveDTO) {
        batchSaveDTO.setShopId(resolveShopId(batchSaveDTO.getShopId()));
        return stockWarningService.batchSave(batchSaveDTO);
    }

    /**
     * 平台管理员可指定主体（不传视为平台自营），店铺与门店强制限定为自身。
     */
    private Integer resolveShopId(Integer requestedShopId) {
        if (AdminTypeEnum.ADMIN.getCode().equals(HeaderUtils.getAdminType())) {
            return requestedShopId == null ? 0 : requestedShopId;
        }
        Integer currentShopId = HeaderUtils.getShopId();
        if (currentShopId == null || currentShopId <= 0) {
            throw new GlobalException("无法获取当前店铺信息");
        }
        return currentShopId;
    }

}
