package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.param.product.ProductStyleEditParam;
import com.tigshop.bean.param.product.ProductStyleItemBatchParam;
import com.tigshop.bean.param.product.ProductStyleSaveParam;
import com.tigshop.bean.query.product.ProductStylePageQuery;
import com.tigshop.bean.vo.product.BrandProductCountVO;
import com.tigshop.bean.vo.product.ProductStyleDetailVO;
import com.tigshop.bean.vo.product.ProductStyleVO;
import com.tigshop.service.product.ProductStyleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.constant.CheckFieldConstants.PRODUCT_STYLE_FIELDS;

@RequiredArgsConstructor
@Tag(name = "商品样式配置")
@RestController
@PreAuthorize("@pms.hasPermission('promotionStyleManage')")
@RequestMapping("/adminapi/promotion/productStyle")
public class ProductStyleController {

    private final ProductStyleService productStyleService;

    @Operation(summary = "商品样式列表")
    @GetMapping("/list")
    public Page<ProductStyleVO> list(ProductStylePageQuery pageQuery) {
        return productStyleService.list(pageQuery);
    }

    @Operation(summary = "商品样式详情")
    @GetMapping("/detail")
    public ProductStyleDetailVO detail(@RequestParam("id") Integer id) {
        return productStyleService.detail(id);
    }

    @Operation(summary = "创建商品样式")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @PostMapping("/create")
    public void create(@RequestBody @Validated ProductStyleSaveParam param) {
        productStyleService.create(param);
    }

    @Operation(summary = "更新商品样式")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @PostMapping("/update")
    public void update(@RequestBody @Validated ProductStyleEditParam param) {
        productStyleService.update(param);
    }

    @PostMapping("/updateField")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @Operation(summary = "更新商品样式字段")
    public void updateField(@RequestBody @Validated UpdateFieldDTO updateField) {
        productStyleService.updateField(updateField, PRODUCT_STYLE_FIELDS);
    }

    @Operation(summary = "删除商品样式")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @PostMapping("/del")
    public void del(@RequestBody @Validated OperateDTO operate) {
        productStyleService.del(operate.getId());
    }

    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @PostMapping("/batch")
    public void batch(@RequestBody @Validated BatchDTO batch) {
        productStyleService.batch(batch);
    }

    @Operation(summary = "批量添加关联商品")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @PostMapping("/addProducts")
    public void addProducts(@RequestBody @Validated ProductStyleItemBatchParam param) {
        productStyleService.addProducts(param);
    }

    @Operation(summary = "批量移除关联商品")
    @PreAuthorize("@pms.hasPermission('promotionStyleModifyManage')")
    @PostMapping("/removeProducts")
    public void removeProducts(@RequestBody @Validated ProductStyleItemBatchParam param) {
        productStyleService.removeProducts(param);
    }

    @Operation(summary = "品牌列表（含商品数量）")
    @GetMapping("/brandList")
    public List<BrandProductCountVO> brandList(@RequestParam(value = "keyword", required = false) String keyword) {
        return productStyleService.brandListWithProductCount(keyword);
    }
}
