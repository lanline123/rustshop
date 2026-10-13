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
import com.tigshop.bean.dto.promotion.PointsExchangeCreateDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeListDTO;
import com.tigshop.bean.dto.promotion.PointsExchangeUpdateDTO;
import com.tigshop.bean.vo.promotion.PointsExchangeVO;
import com.tigshop.service.promotion.PointsExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.POINTS_EXCHANGE_FIELDS;

/**
 * 积分商品控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/pointsExchange")
@Tag(name = "积分商品", description = "积分商品功能")
@PreAuthorize("@pms.hasPermission('pointsExchangeManage')")
@Validated
public class PointsExchangeController {
    @Resource
    private PointsExchangeService pointsExchangeService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<PointsExchangeVO> list(PointsExchangeListDTO listDTO) {
        return pointsExchangeService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public PointsExchangeVO detail(@RequestParam Integer id) {
        return pointsExchangeService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('promotionPointsExchangeModifyManage')")
    public void create(@Valid @RequestBody PointsExchangeCreateDTO createDTO) {
        pointsExchangeService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('promotionPointsExchangeModifyManage')")
    public void update(@Valid @RequestBody PointsExchangeUpdateDTO updateDTO) {
        pointsExchangeService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('promotionPointsExchangeModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        pointsExchangeService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('promotionPointsExchangeModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        pointsExchangeService.updateField(updateField,  POINTS_EXCHANGE_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('promotionPointsExchangeModifyManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        pointsExchangeService.batch(batchDTO);
    }
}
