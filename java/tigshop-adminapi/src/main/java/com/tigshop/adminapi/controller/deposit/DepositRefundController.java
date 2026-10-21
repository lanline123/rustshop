package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositRefundApplyDTO;
import com.tigshop.bean.dto.deposit.DepositRefundAuditDTO;
import com.tigshop.bean.dto.deposit.DepositRefundListDTO;
import com.tigshop.bean.vo.deposit.DepositFullRefundCheckVO;
import com.tigshop.bean.vo.deposit.DepositRefundVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.deposit.DepositRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺保证金退还/提现控制器：店铺申请 + 平台一步式审核（通过即出账完成）。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/refund")
@Tag(name = "店铺保证金退还", description = "店铺保证金退还/提现申请、平台一步式审核（通过即出账完成）")
public class DepositRefundController {

    @Resource
    private DepositRefundService depositRefundService;

    @PostMapping("/apply")
    @Operation(summary = "店铺端发起退还/提现申请")
    public Integer apply(@RequestBody DepositRefundApplyDTO applyDTO) {
        Integer shopId = HeaderUtils.getShopId();
        return depositRefundService.apply(shopId, applyDTO);
    }

    @PostMapping("/vendorApply")
    @Operation(summary = "供应商端发起退还/提现申请")
    public Integer vendorApply(@RequestBody DepositRefundApplyDTO applyDTO) {
        Integer vendorId = HeaderUtils.getVendorId();
        return depositRefundService.vendorApply(vendorId, applyDTO);
    }

    @GetMapping("/fullRefundCheck")
    @Operation(summary = "店铺端查询能否全额退还保证金")
    public DepositFullRefundCheckVO fullRefundCheck() {
        Integer shopId = HeaderUtils.getShopId();
        return depositRefundService.fullRefundCheck(shopId);
    }

    @GetMapping("/vendorFullRefundCheck")
    @Operation(summary = "供应商端查询能否全额退还保证金")
    public DepositFullRefundCheckVO vendorFullRefundCheck() {
        Integer vendorId = HeaderUtils.getVendorId();
        return depositRefundService.vendorFullRefundCheck(vendorId);
    }

    @GetMapping("/list")
    @Operation(summary = "平台端退还单列表")
    @PreAuthorize("@pms.hasPermission('depositPaymentAuditManage')")
    public Page<DepositRefundVO> list(DepositRefundListDTO listDTO) {
        return depositRefundService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "平台端退还单详情")
    @PreAuthorize("@pms.hasPermission('depositPaymentAuditManage')")
    public DepositRefundVO detail(Integer id) {
        return depositRefundService.detail(id);
    }

    @PostMapping("/audit")
    @Operation(summary = "平台端一步式审核退还单(通过即出账完成/驳回)")
    @PreAuthorize("@pms.hasPermission('depositRefundAudit')")
    public void audit(@RequestBody DepositRefundAuditDTO auditDTO) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        depositRefundService.audit(adminId, auditDTO);
    }
}
