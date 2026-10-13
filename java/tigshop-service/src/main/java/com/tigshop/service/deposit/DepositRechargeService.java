package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositPaymentAuditListDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeApplyDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeAuditDTO;
import com.tigshop.bean.dto.deposit.DepositRechargeListDTO;
import com.tigshop.bean.vo.deposit.DepositPaymentAuditVO;
import com.tigshop.bean.vo.deposit.DepositRechargeVO;

/**
 * 店铺保证金充值服务：店铺申请 + 平台审核入账/驳回。
 *
 * <p>状态机（DepositRechargeStatus）：0待审核 →(审核通过) 1已入账 / →(驳回) 2驳回。
 * 审核通过时通过 {@link DepositAccountService#changeDeposit} 以 RECHARGE 入账。</p>
 *
 * @author Tigshop团队
 */
public interface DepositRechargeService {

    /**
     * 店铺端发起充值申请，建充值单 status=待审核。
     *
     * @param shopId   当前登录店铺id（取自登录态）
     * @param applyDTO 充值金额 + 支付凭证
     * @return 新建充值单id
     */
    Integer apply(Integer shopId, DepositRechargeApplyDTO applyDTO);

    /**
     * 供应商端发起充值申请，建充值单 status=待审核（owner_type=供应商）。
     *
     * @param vendorId 当前登录供应商id（取自登录态）
     * @param applyDTO 充值金额 + 支付凭证
     * @return 新建充值单id
     */
    Integer vendorApply(Integer vendorId, DepositRechargeApplyDTO applyDTO);

    /**
     * 平台端分页查询充值单（持有者类型/持有者/状态/时间筛选）。
     */
    Page<DepositRechargeVO> list(DepositRechargeListDTO listDTO);

    /**
     * 平台端查询充值单详情。
     */
    DepositRechargeVO detail(Integer id);

    /**
     * 平台端审核充值单：仅待审核可操作。通过→入账并置已入账；驳回→置驳回并记原因。
     *
     * @param adminId  当前操作管理员id
     * @param auditDTO 审核参数（单id、是否通过、备注）
     */
    void audit(Integer adminId, DepositRechargeAuditDTO auditDTO);

    /**
     * 店铺/供应商端「款项审核」合并列表：按 owner 锁定当前持有者的充值单 + 退还单，
     * 按提交时间倒序混合后分页。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为shopId，供应商为vendorId，取自登录态）
     * @param listDTO   筛选参数（审核类型/状态/时间区间 + 分页）
     * @return 合并分页结果
     */
    Page<DepositPaymentAuditVO> paymentAuditList(Integer ownerType, Integer ownerId, DepositPaymentAuditListDTO listDTO);

    /**
     * 店铺/供应商端「款项审核」详情：按 auditType 取充值单/退还单，并校验单据归属当前登录 owner。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（取自登录态）
     * @param auditType 审核类型 1充值 2退还
     * @param bizId     业务单id（充值单id 或 退还单id）
     * @return 款项审核详情VO
     */
    DepositPaymentAuditVO paymentAuditDetail(Integer ownerType, Integer ownerId, Integer auditType, Integer bizId);
}
