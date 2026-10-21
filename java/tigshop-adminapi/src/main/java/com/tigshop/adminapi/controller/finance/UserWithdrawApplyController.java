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
package com.tigshop.adminapi.controller.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.common.BatchDTO;
import com.tigshop.bean.dto.common.OperateDTO;
import com.tigshop.bean.dto.common.UpdateFieldDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyCreateDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyListDTO;
import com.tigshop.bean.dto.finance.UserWithdrawApplyUpdateDTO;
import com.tigshop.bean.vo.finance.UserWithdrawApplyVO;
import com.tigshop.service.finance.UserWithdrawApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 提现申请控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/userWithdrawApply")
@Tag(name = "提现申请", description = "提现申请功能")
public class UserWithdrawApplyController {
    @Resource
    private UserWithdrawApplyService userWithdrawApplyService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<UserWithdrawApplyVO> list(UserWithdrawApplyListDTO listDTO) {
        return userWithdrawApplyService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public UserWithdrawApplyVO detail(@RequestParam Integer id) {
        return userWithdrawApplyService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('userWithdrawApplyUpdateManage')")
    public void create(@RequestBody UserWithdrawApplyCreateDTO createDTO) {
        userWithdrawApplyService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('userWithdrawApplyUpdateManage')")
    public void update(@RequestBody @Validated UserWithdrawApplyUpdateDTO updateDTO) {
        userWithdrawApplyService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('userWithdrawApplyDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        userWithdrawApplyService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    @PreAuthorize("@pms.hasPermission('userWithdrawApplyUpdateManage')")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        userWithdrawApplyService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('userWithdrawApplyBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        userWithdrawApplyService.batch(batchDTO);
    }
}
