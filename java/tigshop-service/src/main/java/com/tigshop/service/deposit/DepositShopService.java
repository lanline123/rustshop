package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.deposit.DepositShopLogQueryParam;
import com.tigshop.bean.vo.deposit.DepositShopFrozenDetailVO;
import com.tigshop.bean.vo.deposit.DepositShopLogVO;
import com.tigshop.bean.vo.deposit.DepositShopOverviewVO;
import com.tigshop.bean.vo.deposit.DepositShopProtocolVO;
import com.tigshop.bean.vo.deposit.DepositShopWarningVO;

/**
 * 店铺端保证金聚合服务（只读展示）。
 *
 * <p>仅做总览/流水/冻结明细/预警/协议的展示性聚合：账户三数与应缴/已缴通过只读调用
 * {@link DepositAccountService} 现有方法获取，流水/冻结明细查 {@code shop_deposit_log}，
 * 协议读配置键值。本服务不修改账户金额，也不承接充值/退还申请提交（由其它代理实现）。</p>
 *
 * @author Tigshop团队
 */
public interface DepositShopService {

    /**
     * 保证金总览：应缴/已缴/待补缴/可提现/冻结 + 账户状态 + 补缴到期时间 + 是否有协议。
     *
     * @param shopId 当前店铺id（由控制器从店铺端鉴权头解析）
     */
    DepositShopOverviewVO getOverview(Integer shopId);

    /**
     * 店铺端交易流水：按当前 shopId 分页查询 shop_deposit_log（可选 type 筛选）。
     */
    Page<DepositShopLogVO> logList(Integer shopId, DepositShopLogQueryParam param);

    /**
     * 冻结明细 = 平台扣款记录：按当前 shopId 分页查询 shop_deposit_log 中 type=4 平台扣除。
     */
    Page<DepositShopFrozenDetailVO> frozenDetailList(Integer shopId, DepositShopLogQueryParam param);

    /**
     * 首页预警横幅：是否需要预警(待补缴>0)、待补缴金额、补缴截止时间、文案。
     */
    DepositShopWarningVO getWarning(Integer shopId);

    /**
     * 保证金协议：读配置 deposit_protocol_enable / deposit_protocol_content。
     */
    DepositShopProtocolVO getProtocol();
}
