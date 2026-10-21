// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

/*
 * ---------------------------------------------------------------------+
 *  控制器文件 -- 品牌
 * ---------------------------------------------------------------------+
 *  版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 *  作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 *  提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.adminapi.controller.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.product.BrandAuditDTO;
import com.tigshop.bean.dto.product.BrandDTO;
import com.tigshop.bean.dto.product.BrandListDTO;
import com.tigshop.bean.vo.product.BrandSearchVO;
import com.tigshop.bean.vo.product.BrandVO;
import com.tigshop.service.product.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.BRAND_FIELDS;
import static com.tigshop.common.constant.ResultTextConstants.ID_CANNOT_IS_NULL;

/**
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/adminapi/product/brand")
@Tag(name = "品牌")
@Validated
@PreAuthorize("@pms.hasPermission('brandManage','vendorProductManage','productManage')")
public class BrandController {
    @Resource
    private BrandService brandService;

    @GetMapping("/list")
    @Operation(summary = "品牌列表")
    @PreAuthorize("@pms.hasPermission('decorate','brandManage','vendorProductManage','productManage')")
    public Page<BrandVO> list(BrandListDTO brand) {
        return brandService.list(brand);
    }

    @GetMapping("/detail")
    @Operation(summary = "品牌详情")
    public BrandDTO detail(@RequestParam @NotNull(message = ID_CANNOT_IS_NULL) Integer id) {
        return brandService.detail(id);
    }

    @PostMapping("/del")
    @Operation(summary = "删除品牌")
    @PreAuthorize("@pms.hasPermission('brandModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operate) {
        brandService.del(operate.getId());
    }

    @PostMapping("/create")
    @Operation(summary = "创建品牌")
    @PreAuthorize("@pms.hasPermission('brandModifyManage')")
    public String create(@Valid @RequestBody BrandDTO brand) {
        return brandService.create(brand);
    }

    @PostMapping("/update")
    @Operation(summary = "更新品牌")
    @PreAuthorize("@pms.hasPermission('brandModifyManage')")
    public String update(@Valid @RequestBody BrandDTO brand) {
        return brandService.update(brand);
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新品牌字段")
    @PreAuthorize("@pms.hasPermission('brandModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        brandService.updateField(updateField, BRAND_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('brandModifyManage')")
    public void batch(@Valid @RequestBody BatchDTO batch) {
        brandService.batch(batch);
    }

    @GetMapping("/search")
    @Operation(summary = "品牌搜索")
    @PreAuthorize("@pms.hasPermission('decorate','brandManage','vendorProductManage','productManage')")
    public BrandSearchVO search() {
        return brandService.search();
    }


    @PostMapping("/updateFirstWord")
    @Operation(summary = "批量更新首字母")
    public void updateFirstWord() {
        brandService.updateFirstWord();
    }

    @GetMapping("/auditList")
    @Operation(summary = "品牌审核列表")
    public Page<BrandVO> auditList(BrandListDTO dto) {
        dto.setIsAudit(1);
        return brandService.list(dto);
    }

    @PostMapping("/audit")
    @Operation(summary = "品牌审核")
    public String audit(@RequestBody @Valid BrandAuditDTO dto) {
        return brandService.audit(dto);
    }

    @GetMapping("/auditWaitNum")
    @Operation(summary = "获取待审核数量")
    public Long auditWaitNum() {
        return brandService.auditWaitNum();
    }


}
