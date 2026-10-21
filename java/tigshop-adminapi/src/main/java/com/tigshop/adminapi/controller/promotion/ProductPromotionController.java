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
import com.tigshop.bean.dto.promotion.ProductPromotionDTO;
import com.tigshop.bean.dto.promotion.ProductPromotionListDTO;
import com.tigshop.bean.query.promotion.product.ConflictPageQuery;
import com.tigshop.bean.vo.promotion.ProductPromotionConfigVO;
import com.tigshop.bean.vo.promotion.ProductPromotionVO;
import com.tigshop.bean.vo.promotion.product.ConflictPageVO;
import com.tigshop.common.constant.CheckFieldConstants;
import com.tigshop.service.promotion.ProductPromotionService;
import com.tigshop.service.user.UserRankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 满减活动控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/productPromotion")
@Tag(name = "满减活动", description = "满减活动功能")
@Validated
public class ProductPromotionController {
    @Resource
    private ProductPromotionService productPromotionService;

    @Resource
    private UserRankService userRankService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<ProductPromotionVO> list(ProductPromotionListDTO listDTO) {
        return productPromotionService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public ProductPromotionVO detail(@RequestParam Integer id) {
        return productPromotionService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('productPromotionModifyManage')")
    public void create(@Valid @RequestBody ProductPromotionDTO createDTO) {
        productPromotionService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('productPromotionModifyManage')")
    public void update(@Valid @RequestBody ProductPromotionDTO updateDTO) {
        productPromotionService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('productPromotionModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        productPromotionService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('productPromotionModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        productPromotionService.updateField(updateField, CheckFieldConstants.PRODUCT_PROMOTION_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('productPromotionModifyManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        productPromotionService.batch(batchDTO);
    }

    @GetMapping("config")
    @Operation(summary = "获取配置")
    public ProductPromotionConfigVO config() {
        return ProductPromotionConfigVO.builder()
                .promotionStatus(productPromotionService.getConfig())
                .rankList(userRankService.getUserRank())
                .build();
    }

    @GetMapping("/conflict")
    @Operation(summary = "活动冲突列表")
    public Page<ConflictPageVO> conflictPage(ConflictPageQuery query) {
        return productPromotionService.conflictPage(query);
    }

}
