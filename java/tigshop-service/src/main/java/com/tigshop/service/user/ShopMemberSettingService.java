package com.tigshop.service.user;

import com.tigshop.bean.param.user.ShopMemberSettingParam;
import com.tigshop.bean.vo.user.ShopMemberSettingVO;

/**
 * 店铺会员总开关设置服务。
 *
 * <p>参照 {@code WholesaleSettingService}：开关值存 config 表（bizCode=shopMemberEnabled），
 * 读写走 {@code ConfigService}。isEnabled() 供下单绑定与冻结守卫消费。
 */
public interface ShopMemberSettingService {

    /** 读取当前设置 */
    ShopMemberSettingVO get();

    /** 保存总开关 */
    void save(ShopMemberSettingParam param);

    /** 总开关是否开启 */
    boolean isEnabled();

    /** 店铺客户信息脱敏开关是否开启 */
    boolean isDesensitizeEnabled();

    /**
     * 冻结守卫：店铺端（getShopId()>0）在总开关关闭时禁止会员相关写操作。
     * 平台端（getShopId()==0）不受影响。
     *
     * @throws com.tigshop.common.exception.GlobalException 店铺端且总开关关闭时抛出
     */
    void assertEnabledForShop();

    /**
     * 店铺添加客户守卫：开关关闭时禁止店铺端新增客户。
     *
     * @throws com.tigshop.common.exception.GlobalException 开关关闭时抛出
     */
    void assertAddEnabled();

    /**
     * 店铺导入客户守卫：开关关闭时禁止店铺端导入客户。
     *
     * @throws com.tigshop.common.exception.GlobalException 开关关闭时抛出
     */
    void assertImportEnabled();
}
