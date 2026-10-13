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
import com.tigshop.bean.dto.finance.UserRechargeOrderCreateDTO;
import com.tigshop.bean.dto.finance.UserRechargeOrderListDTO;
import com.tigshop.bean.dto.finance.UserRechargeOrderUpdateDTO;
import com.tigshop.bean.vo.finance.UserRechargeOrderVO;
import com.tigshop.service.finance.UserRechargeOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.tigshop.common.constant.CheckFieldConstants.GENERAL_FIELDS;

/**
 * 充值记录控制器
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:30
 */
@RestController
@RequestMapping("/adminapi/finance/userRechargeOrder")
@Tag(name = "充值记录", description = "充值记录功能")
public class UserRechargeOrderController {
    @Resource
    private UserRechargeOrderService userRechargeOrderService;

    @GetMapping("/list")
    @Operation(summary = "获取列表")
    public Page<UserRechargeOrderVO> list(UserRechargeOrderListDTO listDTO) {
        return userRechargeOrderService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "获取详情")
    public UserRechargeOrderVO detail(@RequestParam Integer id) {
        return userRechargeOrderService.detail(id);
    }

    @PostMapping("/create")
    @Operation(summary = "创建")
    @PreAuthorize("@pms.hasPermission('userRechargeOrderUpdateManage')")
    public void create(@RequestBody UserRechargeOrderCreateDTO createDTO) {
        userRechargeOrderService.create(createDTO);
    }

    @PostMapping("/update")
    @Operation(summary = "更新")
    @PreAuthorize("@pms.hasPermission('userRechargeOrderUpdateManage')")
    public void update(@RequestBody UserRechargeOrderUpdateDTO updateDTO) {
        userRechargeOrderService.update(updateDTO);
    }

    @PostMapping("/del")
    @Operation(summary = "删除")
    @PreAuthorize("@pms.hasPermission('userRechargeOrderDelManage')")
    public void del(@RequestBody OperateDTO operateDTO) {
        userRechargeOrderService.del(operateDTO.getId());
    }

    @PostMapping("/updateField")
    @Operation(summary = "更新字段")
    public void updateField(@RequestBody UpdateFieldDTO updateField) {
        userRechargeOrderService.updateField(updateField, GENERAL_FIELDS);
    }

    @PostMapping("/batch")
    @Operation(summary = "批量操作")
    @PreAuthorize("@pms.hasPermission('userRechargeOrderBatchManage')")
    public void batch(@RequestBody BatchDTO batchDTO) {
        userRechargeOrderService.batch(batchDTO);
    }
}
