package com.tigshop.service.deposit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.deposit.DepositShopLogQueryParam;
import com.tigshop.bean.vo.deposit.DepositShopFrozenDetailVO;
import com.tigshop.bean.vo.deposit.DepositShopLogVO;
import com.tigshop.bean.vo.deposit.DepositShopOverviewVO;
import com.tigshop.bean.vo.deposit.DepositShopProtocolVO;
import com.tigshop.bean.vo.deposit.DepositShopWarningVO;

/**
 * 供应商端保证金聚合服务（只读展示）。
 *
 * <p>与店铺端 {@link DepositShopService} 平行，但持有者维度为供应商（owner_type=SUPPLIER，
 * owner_id=vendorId）：仅做总览/流水/冻结明细/预警/协议的展示性聚合。账户三数与应缴/已缴
 * 通过只读调用 {@link DepositAccountService} 的 owner 版方法获取，流水/冻结明细查
 * {@code shop_deposit_log}（ownerType=SUPPLIER），协议读配置键值。本服务不修改账户金额，
 * 也不承接充值/退还申请提交（由其它代理实现）。复用店铺端 VO 与 param 以避免重复结构。</p>
 *
 * @author Tigshop团队
 */
public interface VendorDepositShopService {

    /**
     * 保证金总览：应缴/已缴/待补缴/可提现/冻结 + 账户状态 + 补缴到期时间 + 是否有协议。
     *
     * @param vendorId 当前供应商id（由控制器从供应商端鉴权头解析）
     */
    DepositShopOverviewVO getOverview(Integer vendorId);

    /**
     * 供应商端交易流水：按当前 vendorId 分页查询 shop_deposit_log（owner_type=SUPPLIER，可选 type 筛选）。
     */
    Page<DepositShopLogVO> logList(Integer vendorId, DepositShopLogQueryParam param);

    /**
     * 冻结明细 = 平台扣款记录：按当前 vendorId 分页查询 shop_deposit_log 中 type=4 平台扣除。
     */
    Page<DepositShopFrozenDetailVO> frozenDetailList(Integer vendorId, DepositShopLogQueryParam param);

    /**
     * 首页预警横幅：是否需要预警(待补缴>0)、待补缴金额、补缴截止时间、文案。
     */
    DepositShopWarningVO getWarning(Integer vendorId);

    /**
     * 保证金协议：读配置 deposit_protocol_enable / deposit_protocol_content。
     */
    DepositShopProtocolVO getProtocol();
}
