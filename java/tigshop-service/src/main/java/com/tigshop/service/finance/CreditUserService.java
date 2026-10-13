package com.tigshop.service.finance;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.finance.CreditUserCreateDTO;
import com.tigshop.bean.dto.finance.CreditUserListDTO;
import com.tigshop.bean.dto.finance.CreditUserUpdateDTO;
import com.tigshop.bean.model.finance.CreditUser;
import com.tigshop.bean.vo.finance.CreditUserListVO;
import com.tigshop.bean.vo.finance.CreditUserVO;
import com.tigshop.bean.vo.finance.UserCreditDetailVO;
import com.tigshop.bean.vo.finance.UserCreditInfoVO;
import com.tigshop.service.common.BaseService;

import java.math.BigDecimal;

/**
 * 授信会员服务接口
 *
 * @author Tigshop团队
 * @create 2026-01-14
 */
public interface CreditUserService extends BaseService<CreditUser> {

    /**
     * 授信会员列表
     *
     * @param listDTO 查询参数
     * @return 分页列表
     */
    Page<CreditUserListVO> list(CreditUserListDTO listDTO);

    /**
     * 授信会员详情
     *
     * @param creditUserId 授信会员ID
     * @return 详情
     */
    CreditUserVO detail(Integer creditUserId);

    /**
     * 添加授信会员
     *
     * @param createDTO 创建参数
     * @return 是否成功
     */
    boolean create(CreditUserCreateDTO createDTO);

    /**
     * 修改授信会员
     *
     * @param updateDTO 更新参数
     * @return 是否成功
     */
    boolean update(CreditUserUpdateDTO updateDTO);

    /**
     * 删除授信会员
     *
     * @param creditUserId 授信会员ID
     * @return 是否成功
     */
    boolean delete(Integer creditUserId);

    /**
     * 禁用/启用授信会员
     *
     * @param creditUserId 授信会员ID
     * @param status       状态：0-禁用，1-启用
     */
    void setStatus(Integer creditUserId, Integer status);

    /**
     * 根据用户ID获取授信会员
     *
     * @param userId 用户ID
     * @return 授信会员
     */
    CreditUser getByUserId(Integer userId);

    /**
     * 扣减额度
     *
     * @param userId 用户ID
     * @param amount 扣减金额
     * @param remark 备注
     */
    void deductAmount(Integer userId, BigDecimal amount, String remark);

    /**
     * 恢复额度
     *
     * @param userId 用户ID
     * @param amount 恢复金额
     * @param remark 备注
     */
    void restoreAmount(Integer userId, BigDecimal amount, String remark);

    /**
     * 获取用户账期支付信息（用于订单确认页）
     *
     * @param userId      用户ID
     * @param orderAmount 订单金额
     * @return 账期支付信息
     */
    UserCreditInfoVO getUserCreditInfo(Integer userId, BigDecimal orderAmount);

    /**
     * 获取用户账期支付信息详情（含未结算/已结算订单列表）
     *
     * @param userId 用户ID
     * @return 用户账期支付信息详情
     */
    UserCreditDetailVO getUserCreditDetail(Integer userId);

    /**
     * 校验用户是否可以使用账期支付
     *
     * @param userId     用户ID
     * @param creditUser 授信会员信息
     * @return 是否可使用
     */
    boolean canUseCreditPayment(Integer userId, CreditUser creditUser);

    /**
     * 当用户结算清空所有待结算账期订单后，重置结算周期起点
     *
     * @param userId 用户ID
     */
    void resetPeriodStartIfNoPending(Integer userId);

    /**
     * 定时任务：更新所有授信会员的周期起点
     * 如果当前时间已超过周期结束时间，则将periodStartTime更新为新周期起点
     */
    void updateAllPeriodStartTime();
}
