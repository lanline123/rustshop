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
import com.tigshop.bean.dto.promotion.PromotionCreateDTO;
import com.tigshop.bean.dto.promotion.PromotionListDTO;
import com.tigshop.bean.dto.promotion.PromotionUpdateDTO;
import com.tigshop.bean.vo.promotion.PromotionCountVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.service.promotion.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 营销管理控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/promotion")
@Tag(name = "营销管理", description = "营销管理功能")
@PreAuthorize("@pms.hasPermission('promotionManage')")
@Validated
public class PromotionController {
    @Resource
    private PromotionService promotionService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PromotionVO> list(PromotionListDTO listDTO) {
        return promotionService.list(listDTO);
    }

    @GetMapping("/getPromotionCount")
    @Operation(summary = "获取活动数量")
    public PromotionCountVO getPromotionCount() {
        return promotionService.getCount();
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    public void create(@Valid @RequestBody PromotionCreateDTO createDTO) {
        promotionService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    public void update(@Valid @RequestBody PromotionUpdateDTO updateDTO) {
        promotionService.update(updateDTO);
    }
}
