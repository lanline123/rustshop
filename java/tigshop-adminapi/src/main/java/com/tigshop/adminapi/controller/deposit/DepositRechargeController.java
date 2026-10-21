package com.tigshop.adminapi.controller.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositPaymentAuditListDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeApplyDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeAuditDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeListDTO;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.vo.deposit.DepositPaymentAuditVO;
import com.tigshop.bean.vo.deposit.DepositPayeeAccountVO;
import com.tigshop.bean.vo.deposit.DepositRechargeVO;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.deposit.DepositPayeeAccountService;
import com.tigshop.service.deposit.DepositRechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺保证金充值控制器：店铺申请 + 平台审核。
 *
 * @author Tigshop团队
 */
@RestController
@RequestMapping("/adminapi/deposit/recharge")
@Tag(name = "店铺保证金充值", description = "店铺保证金充值申请与平台审核")
public class DepositRechargeController {

    @Resource
    private DepositRechargeService depositRechargeService;

    @Resource
    private DepositPayeeAccountService depositPayeeAccountService;

    @GetMapping("/payeeAccountOptions")
    @Operation(summary = "充值可选收款账户列表(店铺/供应商端，仅启用)")
    public List<DepositPayeeAccountVO> payeeAccountOptions(
            @RequestParam(required = false) Integer accountType,
            @RequestParam(required = false) String keyword) {
        return depositPayeeAccountService.listEnabled(accountType, keyword);
    }

    @PostMapping("/apply")
    @Operation(summary = "店铺端发起充值申请")
    public Integer apply(@RequestBody DepositRechargeApplyDTO applyDTO) {
        Integer shopId = HeaderUtils.getShopId();
        return depositRechargeService.apply(shopId, applyDTO);
    }

    @PostMapping("/vendorApply")
    @Operation(summary = "供应商端发起充值申请")
    public Integer vendorApply(@RequestBody DepositRechargeApplyDTO applyDTO) {
        Integer vendorId = HeaderUtils.getVendorId();
        return depositRechargeService.vendorApply(vendorId, applyDTO);
    }

    @GetMapping("/shopPaymentAuditList")
    @Operation(summary = "店铺端款项审核列表(充值+退还合并，按提交时间倒序)")
    public Page<DepositPaymentAuditVO> shopPaymentAuditList(DepositPaymentAuditListDTO listDTO) {
        Integer shopId = HeaderUtils.getShopId();
        return depositRechargeService.paymentAuditList(DepositOwnerType.SHOP.getCode(), shopId, listDTO);
    }

    @GetMapping("/vendorPaymentAuditList")
    @Operation(summary = "供应商端款项审核列表(充值+退还合并，按提交时间倒序)")
    public Page<DepositPaymentAuditVO> vendorPaymentAuditList(DepositPaymentAuditListDTO listDTO) {
        Integer vendorId = HeaderUtils.getVendorId();
        return depositRechargeService.paymentAuditList(DepositOwnerType.SUPPLIER.getCode(), vendorId, listDTO);
    }

    @GetMapping("/shopPaymentAuditDetail")
    @Operation(summary = "店铺端款项审核详情(按auditType取充值/退还单，校验归属)")
    public DepositPaymentAuditVO shopPaymentAuditDetail(@RequestParam Integer auditType,
                                                        @RequestParam Integer bizId) {
        Integer shopId = HeaderUtils.getShopId();
        return depositRechargeService.paymentAuditDetail(DepositOwnerType.SHOP.getCode(), shopId, auditType, bizId);
    }

    @GetMapping("/vendorPaymentAuditDetail")
    @Operation(summary = "供应商端款项审核详情(按auditType取充值/退还单，校验归属)")
    public DepositPaymentAuditVO vendorPaymentAuditDetail(@RequestParam Integer auditType,
                                                          @RequestParam Integer bizId) {
        Integer vendorId = HeaderUtils.getVendorId();
        return depositRechargeService.paymentAuditDetail(DepositOwnerType.SUPPLIER.getCode(), vendorId, auditType, bizId);
    }

    @GetMapping("/list")
    @Operation(summary = "平台端充值单列表")
    @PreAuthorize("@pms.hasPermission('depositPaymentAuditManage')")
    public Page<DepositRechargeVO> list(DepositRechargeListDTO listDTO) {
        return depositRechargeService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "平台端充值单详情")
    @PreAuthorize("@pms.hasPermission('depositPaymentAuditManage')")
    public DepositRechargeVO detail(Integer id) {
        return depositRechargeService.detail(id);
    }

    @PostMapping("/audit")
    @Operation(summary = "平台端审核充值单(通过入账/驳回)")
    @PreAuthorize("@pms.hasPermission('depositRechargeAudit')")
    public void audit(@RequestBody DepositRechargeAuditDTO auditDTO) {
        Integer adminId = SecurityUtils.getCurrentAdminId();
        depositRechargeService.audit(adminId, auditDTO);
    }
}
