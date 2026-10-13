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
import com.tigshop.bean.dto.promotion.RechargeSettingCreateDTO;
import com.tigshop.bean.dto.promotion.RechargeSettingListDTO;
import com.tigshop.bean.dto.promotion.RechargeSettingUpdateDTO;
import com.tigshop.bean.vo.promotion.RechargeSettingVO;
import com.tigshop.service.promotion.RechargeSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.RECHARGE_SETTING_FIELDS;

/**
 * 充值余额控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/promotion/rechargeSetting")
@Tag(name = "充值余额", description = "充值余额功能")
@PreAuthorize("@pms.hasPermission('rechargeSettingManage')")
@Validated
public class RechargeSettingController {
    @Resource
    private RechargeSettingService rechargeSettingService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<RechargeSettingVO> list(RechargeSettingListDTO listDTO) {
        return rechargeSettingService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public RechargeSettingVO detail(@RequestParam Integer id) {
        return rechargeSettingService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('rechargeSettingModifyManage')")
    public void create(@Valid @RequestBody RechargeSettingCreateDTO createDTO) {
        rechargeSettingService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('rechargeSettingModifyManage')")
    public void update(@Valid @RequestBody RechargeSettingUpdateDTO updateDTO) {
        rechargeSettingService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('rechargeSettingModifyManage')")
    public void del(@Valid @RequestBody OperateDTO operateDTO) {
        rechargeSettingService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('rechargeSettingModifyManage')")
    public void updateField(@Valid @RequestBody UpdateFieldDTO updateField) {
        rechargeSettingService.updateField(updateField, RECHARGE_SETTING_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('rechargeSettingModifyManage')")
    public void batch(@Valid @RequestBody BatchDTO batchDTO) {
        rechargeSettingService.batch(batchDTO);
    }
}
