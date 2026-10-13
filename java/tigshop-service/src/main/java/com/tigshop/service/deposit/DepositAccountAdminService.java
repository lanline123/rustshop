package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.deposit.DepositAccountListDTO;
import com.tigshop.bean.dto.deposit.DepositAdjustDTO;
import com.tigshop.bean.dto.deposit.DepositDeductDTO;
import com.tigshop.bean.dto.deposit.DepositSendWarningDTO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.bean.vo.deposit.DepositAccountDetailVO;
import com.tigshop.bean.vo.deposit.DepositAccountVO;
import com.tigshop.bean.vo.deposit.DepositAdjustLogVO;
import com.tigshop.bean.vo.deposit.DepositLogVO;

/**
 * 平台端店铺保证金账户管理服务（W3：账户查询/详情/手动调整应缴/赔付扣除/发送预警/交易记录）。
 *
 * <p>本服务为平台管理端业务编排层，只读调用地基 {@link DepositAccountService} 获取应缴/已缴/三数与记账，
 * 自身用 Mapper 完成手动调整应缴的事务写入；不修改地基记账核心。</p>
 *
 * @author Tigshop团队
 */
public interface DepositAccountAdminService {

    /**
     * 账户分页总览。列：店铺名/商户名/经营类目/应缴/已交/待补缴/保证金状态。
     * 筛选：店铺名、商户名、经营类目、保证金状态、入驻时间。
     */
    Page<DepositAccountVO> list(DepositAccountListDTO listDTO);

    /**
     * 账户详情-基本信息（店铺/供应商类型/经营类目/开店时间/所属商户/保证金状态/应缴-已缴-待缴）。
     * 按 (ownerType, ownerId) 维度查询，ownerType 为空时默认店铺(1)。
     */
    DepositAccountDetailVO detail(Integer ownerType, Integer ownerId);

    /**
     * 详情-交易记录（按 (ownerType, ownerId) 查 shop_deposit_log），ownerType 为空时默认店铺(1)。
     */
    Page<DepositLogVO> logList(Integer ownerType, Integer ownerId, BasePage page);

    /**
     * 详情-调整记录（按 (ownerType, ownerId) 查 shop_deposit_adjust_log），ownerType 为空时默认店铺(1)。
     */
    Page<DepositAdjustLogVO> adjustLogList(Integer ownerType, Integer ownerId, BasePage page);

    /**
     * 手动调整应缴。事务包裹：读旧应缴 → 设/清 requiredManual → 写调整记录。
     *
     * @param dto     调整参数
     * @param adminId 操作管理员id
     */
    void adjust(DepositAdjustDTO dto, Integer adminId);

    /**
     * 赔付扣除。调用地基 changeDeposit(DEDUCT) 扣减实缴。
     *
     * @param dto     扣除参数
     * @param adminId 操作管理员id
     */
    void deduct(DepositDeductDTO dto, Integer adminId);

    /**
     * 发送保证金预警（站内信，发往目标店铺）。
     *
     * @param dto     预警参数
     * @param adminId 操作管理员id
     */
    void sendWarning(DepositSendWarningDTO dto, Integer adminId);
}
