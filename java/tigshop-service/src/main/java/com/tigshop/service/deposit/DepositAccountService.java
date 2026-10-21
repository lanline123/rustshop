package com.tigshop.service.deposit;

import com.tigshop.bean.enums.deposit.DepositLogType;
import com.tigshop.bean.enums.deposit.DepositOwnerType;
import com.tigshop.bean.model.deposit.ShopDeposit;

import java.math.BigDecimal;

/**
 * 保证金账户服务（W1 地基：记账核心 + 应缴口径计算）。统一 owner 维度（店铺 + 供应商）。
 *
 * <p>账户口径（A 方案）：账户存实缴 paid + 手动应缴覆盖值 requiredManual（NULL=按规则实时算）
 * + 双缓冲到期时间。应缴 R：若 requiredManual 非空取之；否则按
 * (主体类型 × 持有者类型 × 在售商品类目三级就近 × 当前生效期) 命中规则全店取最高（无命中/无在售=0）。
 * 冻结=min(paid,R)、可用=待提现=max(0,paid-R)、待补缴=max(0,R-paid)。</p>
 *
 * <p>owner 维度：owner_type 1店铺(SHOP) 2供应商(SUPPLIER)；owner_id 店铺为 shopId，供应商为 vendorId。
 * 主签名按 (ownerType, ownerId) 维度；同时保留 shopId 单参兼容重载（内部委托为
 * {@code ownerType=SHOP(1)}），使已有店铺维度业务代码零返工继续编译。</p>
 *
 * @author Tigshop团队
 */
public interface DepositAccountService {

    // ====================== owner 维度主签名 ======================

    /**
     * 获取保证金账户，不存在则初始化一条
     * （paid=0、requiredManual=null、collectDueTime=0、supplementDueTime=0）。
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     */
    ShopDeposit getOrCreate(Integer ownerType, Integer ownerId);

    /**
     * 记账核心：唯一修改 paid 的入口。事务 + 行锁 + 写流水。
     *
     * <p>RECHARGE 入账（+paid）；WITHDRAW/REFUND/DEDUCT 出账（-paid，余额不足抛异常）；
     * ADJUST 平台调整（双向，amount 为带符号金额，可正可负，直接加到 paid，调整后 paid 不可为负）。</p>
     *
     * @param ownerType 持有者类型 1店铺 2供应商
     * @param ownerId   持有者id（店铺为 shopId，供应商为 vendorId）
     * @param amount    变动金额（RECHARGE/WITHDRAW/REFUND/DEDUCT 传正数；ADJUST 传带符号金额，可正可负）
     * @param type      流水类型
     * @param relatedId 关联单id（充值/退还单等），无则传 0
     * @param orderId   关联订单id（平台扣除/赔付时），无则传 0
     * @param adminId   操作管理员id，无则传 0
     * @param remark    备注
     */
    void changeDeposit(Integer ownerType, Integer ownerId, BigDecimal amount, DepositLogType type,
                       Integer relatedId, Integer orderId, Integer adminId, String remark);

    /**
     * 应缴 = 手动应缴覆盖值优先（requiredManual 非空取之）；否则按
     * (主体类型 × 持有者类型 × 在售商品类目三级就近 × 当前生效期) 命中规则全店取最高，无在售/无命中返回 0。
     */
    BigDecimal computeRequired(Integer ownerType, Integer ownerId);

    /**
     * 实缴余额（账户不存在返回 0）。
     */
    BigDecimal getPaid(Integer ownerType, Integer ownerId);

    /**
     * 冻结金额 = min(paid, required)。
     */
    BigDecimal frozen(Integer ownerType, Integer ownerId);

    /**
     * 可用余额 = max(0, paid - required)。
     */
    BigDecimal available(Integer ownerType, Integer ownerId);

    /**
     * 待补缴 = max(0, required - paid)。
     */
    BigDecimal pending(Integer ownerType, Integer ownerId);

    /**
     * 可提现上限 = max(0, paid - required)。
     */
    BigDecimal withdrawable(Integer ownerType, Integer ownerId);

    // ====================== shopId 兼容重载（委托为 ownerType=SHOP） ======================

    /**
     * 店铺维度兼容重载：等价于 {@code getOrCreate(SHOP, shopId)}。
     */
    default ShopDeposit getOrCreate(Integer shopId) {
        return getOrCreate(DepositOwnerType.SHOP.getCode(), shopId);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code changeDeposit(SHOP, shopId, ...)}。
     */
    default void changeDeposit(Integer shopId, BigDecimal amount, DepositLogType type,
                               Integer relatedId, Integer orderId, Integer adminId, String remark) {
        changeDeposit(DepositOwnerType.SHOP.getCode(), shopId, amount, type, relatedId, orderId, adminId, remark);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code computeRequired(SHOP, shopId)}。
     */
    default BigDecimal computeRequired(Integer shopId) {
        return computeRequired(DepositOwnerType.SHOP.getCode(), shopId);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code getPaid(SHOP, shopId)}。
     */
    default BigDecimal getPaid(Integer shopId) {
        return getPaid(DepositOwnerType.SHOP.getCode(), shopId);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code frozen(SHOP, shopId)}。
     */
    default BigDecimal frozen(Integer shopId) {
        return frozen(DepositOwnerType.SHOP.getCode(), shopId);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code available(SHOP, shopId)}。
     */
    default BigDecimal available(Integer shopId) {
        return available(DepositOwnerType.SHOP.getCode(), shopId);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code pending(SHOP, shopId)}。
     */
    default BigDecimal pending(Integer shopId) {
        return pending(DepositOwnerType.SHOP.getCode(), shopId);
    }

    /**
     * 店铺维度兼容重载：等价于 {@code withdrawable(SHOP, shopId)}。
     */
    default BigDecimal withdrawable(Integer shopId) {
        return withdrawable(DepositOwnerType.SHOP.getCode(), shopId);
    }
}
