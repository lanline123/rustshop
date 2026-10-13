package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositRefundApplyDTO;
import com.tigshop.bean.dto.deposit.DepositRefundAuditDTO;
import com.tigshop.bean.dto.deposit.DepositRefundListDTO;
import com.tigshop.bean.vo.deposit.DepositFullRefundCheckVO;
import com.tigshop.bean.vo.deposit.DepositRefundVO;

import java.math.BigDecimal;

/**
 * 店铺保证金退还/提现服务：店铺申请 + 平台一步式审核（通过即出账完成）。
 *
 * <p>状态机（DepositRefundStatus）：0待审核 →(审核通过，出账+记打款凭证) 3完成 / 审核驳回→ 2驳回。
 * 申请校验可退金额 = max(0, withdrawable − 待审核退还总和)，超额抛 DEPOSIT_WITHDRAW_EXCEED_LIMIT。
 * 审核通过时按退还类型（1提现/2退还）通过 {@link DepositAccountService#changeDeposit} 出账。</p>
 *
 * @author Tigshop团队
 */
public interface DepositRefundService {

    /**
     * 店铺端发起退还/提现申请，建退还单 status=待审核。
     * 校验：申请金额 ≤ max(0, withdrawable(shopId) − 待审核退还总和)。
     *
     * @param shopId   当前登录店铺id（取自登录态）
     * @param applyDTO 退还类型 + 金额 + 收款信息/原因
     * @return 新建退还单id
     */
    Integer apply(Integer shopId, DepositRefundApplyDTO applyDTO);

    /**
     * 供应商端发起退还/提现申请，建退还单 status=待审核（owner_type=供应商）。
     * 校验：申请金额 ≤ max(0, withdrawable(SUPPLIER, vendorId) − 待审核退还总和)。
     *
     * @param vendorId 当前登录供应商id（取自登录态）
     * @param applyDTO 退还类型 + 金额 + 收款信息/原因
     * @return 新建退还单id
     */
    Integer vendorApply(Integer vendorId, DepositRefundApplyDTO applyDTO);

    /**
     * 店铺端预检：当前店铺能否全额退还保证金。
     *
     * <p>只读、不抛异常；按全额退还前置条件评估，其中"末单确认收货满 N 天"的 N
     * 取售后限制天数配置（{@code afterSalesLimitDays}）。可退时返回可退金额，
     * 不可退时金额为 0 并附未满足原因。</p>
     *
     * @param shopId 当前登录店铺id（取自登录态）
     */
    DepositFullRefundCheckVO fullRefundCheck(Integer shopId);

    /**
     * 供应商端预检：当前供应商能否全额退还保证金。口径同 {@link #fullRefundCheck(Integer)}，
     * 仅 owner 维度为供应商。
     *
     * @param vendorId 当前登录供应商id（取自登录态）
     */
    DepositFullRefundCheckVO vendorFullRefundCheck(Integer vendorId);

    /**
     * 平台端分页查询退还单（持有者类型/持有者/类型/状态/时间筛选）。
     */
    Page<DepositRefundVO> list(DepositRefundListDTO listDTO);

    /**
     * 平台端查询退还单详情。
     */
    DepositRefundVO detail(Integer id);

    /**
     * 平台端一步式审核退还单：仅待审核可操作。
     * 通过→按退还类型出账并置完成（打款凭证必填）；驳回→置驳回并记原因。
     *
     * @param adminId  当前操作管理员id
     * @param auditDTO 审核参数（单id、是否通过、打款凭证、备注）
     */
    void audit(Integer adminId, DepositRefundAuditDTO auditDTO);

    /**
     * 待审核退还款项总和：owner 维度下 status=待审核 的退还单金额（含提现/全额退还）之和。
     *
     * <p>用于计算有效可退额度：有效可退 = max(0, withdrawable − 待审核退还总和)，
     * 防止多次申请累计超过实际可退余额；供退还申请校验、退还预检与账户总览统一复用。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     * @return 待审核退还金额之和（无则返回 0）
     */
    BigDecimal pendingRefundSum(Integer ownerType, Integer ownerId);
}
