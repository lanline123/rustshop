package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositAccountListDTO;
import com.tigshop.bean.dto.deposit.DepositAdjustDTO;
import com.tigshop.bean.dto.deposit.DepositDeductDTO;
import com.tigshop.bean.dto.deposit.DepositSendWarningDTO;
import com.tigshop.bean.vo.deposit.DepositAccountDetailVO;
import com.tigshop.bean.vo.deposit.DepositAccountVO;
import com.tigshop.bean.vo.deposit.DepositAdjustLogVO;
import com.tigshop.bean.vo.deposit.DepositLogVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.deposit.DepositAccountAdminService;
import com.tigshop.service.deposit.task.DepositDueCheckTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台端店铺保证金账户控制器（账户查询/详情/交易记录/调整记录/手动调整应缴/赔付扣除/发送预警）。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/account")
@Tag(name = "店铺保证金账户", description = "平台端店铺保证金账户管理")
@PreAuthorize("@pms.hasPermission('depositAccountManage')")
public class DepositAccountController {

    @Resource
    private DepositAccountAdminService depositAccountAdminService;

    @Resource
    private DepositDueCheckTask depositDueCheckTask;

    @GetMapping("/list")
    @Operation(summary = "账户分页总览")
    public Page<DepositAccountVO> list(DepositAccountListDTO listDTO) {
        return depositAccountAdminService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "账户详情-基本信息")
    public DepositAccountDetailVO detail(Integer ownerType, Integer ownerId, Integer shopId) {
        return depositAccountAdminService.detail(ownerType, resolveOwnerId(ownerId, shopId));
    }

    @GetMapping("/logList")
    @Operation(summary = "详情-交易记录")
    public Page<DepositLogVO> logList(Integer ownerType, Integer ownerId, Integer shopId, BasePage page) {
        return depositAccountAdminService.logList(ownerType, resolveOwnerId(ownerId, shopId), page);
    }

    @GetMapping("/adjustLogList")
    @Operation(summary = "详情-调整记录")
    public Page<DepositAdjustLogVO> adjustLogList(Integer ownerType, Integer ownerId, Integer shopId, BasePage page) {
        return depositAccountAdminService.adjustLogList(ownerType, resolveOwnerId(ownerId, shopId), page);
    }

    @PostMapping("/adjust")
    @Operation(summary = "手动调整应缴")
    @PreAuthorize("@pms.hasPermission('depositAdjust')")
    public void adjust(@RequestBody DepositAdjustDTO dto) {
        depositAccountAdminService.adjust(dto, SecurityUtils.getCurrentAdminId());
    }

    @PostMapping("/deduct")
    @Operation(summary = "赔付扣除")
    @PreAuthorize("@pms.hasPermission('depositDeduct')")
    public void deduct(@RequestBody DepositDeductDTO dto) {
        depositAccountAdminService.deduct(dto, SecurityUtils.getCurrentAdminId());
    }

    @PostMapping("/sendWarning")
    @Operation(summary = "发送保证金预警")
    @PreAuthorize("@pms.hasPermission('depositSendWarning')")
    public void sendWarning(@RequestBody DepositSendWarningDTO dto) {
        depositAccountAdminService.sendWarning(dto, SecurityUtils.getCurrentAdminId());
    }

    @GetMapping("/runDueCheck")
    @Operation(summary = "手动触发保证金到期扫描(测试/运维用,免登录免鉴权)")
    @PreAuthorize("permitAll()")
    public void runDueCheck() {
        depositDueCheckTask.manualExecute();
    }

    /**
     * owner id 兼容解析：优先 ownerId，回退兼容字段 shopId（历史仅传 shopId 的店铺维度调用）。
     */
    private Integer resolveOwnerId(Integer ownerId, Integer shopId) {
        return ownerId != null ? ownerId : shopId;
    }
}
