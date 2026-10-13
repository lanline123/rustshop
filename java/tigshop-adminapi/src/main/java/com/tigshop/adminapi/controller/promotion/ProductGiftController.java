// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.promotion.ProductGiftCreateDTO;
import com.tigshop.bean.dto.promotion.ProductGiftListDTO;
import com.tigshop.bean.dto.promotion.ProductGiftUpdateDTO;
import com.tigshop.bean.vo.promotion.ProductGiftVO;
import com.tigshop.service.promotion.ProductGiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 活动赠品控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/productGift")
@Tag(name = "活动赠品", description = "活动赠品功能")
@PreAuthorize("@pms.hasPermission('productGiftManage')")
@Validated
public class ProductGiftController {
    @Resource
    private ProductGiftService productGiftService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ProductGiftVO> list(ProductGiftListDTO listDTO) {
        return productGiftService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ProductGiftVO detail(@RequestParam("giftId") Integer giftId) {
        return productGiftService.detail(giftId);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('productGiftModifyManage')")
    public void create(@Valid @RequestBody ProductGiftCreateDTO createDTO) {
        productGiftService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('productGiftModifyManage')")
    public void update(@Valid @RequestBody ProductGiftUpdateDTO updateDTO) {
        productGiftService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('productGiftModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        productGiftService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('productGiftModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        productGiftService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('productGiftModifyManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        productGiftService.batch(batchDTO);
    }
}
