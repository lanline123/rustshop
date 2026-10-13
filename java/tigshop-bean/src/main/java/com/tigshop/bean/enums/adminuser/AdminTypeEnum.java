// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.enums.adminuser;

import com.tigshop.bean.enums.o2o.ShopTypeEnum;
import lombok.Getter;

/**
 * 店铺类型
 *
 * @author Jayce
 * @create 2024年11月20日 15:41
 */
@Getter
public enum AdminTypeEnum {
    ADMIN("admin", "管理后台", "平台超级管理员"),
    SHOP("shop", "店铺", "店铺超级管理员"),
    VENDOR("vendor", "供应商", "供应商超级管理员"),
    STORE("store", "门店", "门店超级管理员"),
    PICKUP("pickup", "自提点", "自提点超级管理员"),
    POS("pos", "收银台", "POS收银台超级管理员"),
    ;

    private final String code;
    private final String desc;
    private final String systemSuperRoleName;

    AdminTypeEnum(String code, String desc, String systemSuperRoleName) {
        this.code = code;
        this.desc = desc;
        this.systemSuperRoleName = systemSuperRoleName;
    }

    public static AdminTypeEnum fromCode(String code) {
        for (AdminTypeEnum type : AdminTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效 adminType: " + code);
    }

    /**
     * 根据店铺类型解析后台管理员类型（内置超级管理员角色用）
     */
    public static String resolveCodeByShopType(Integer shopType) {
        if (shopType == null) {
            return SHOP.getCode();
        }
        if (shopType == ShopTypeEnum.STORE.getCode()) {
            return STORE.getCode();
        }
        if (shopType == ShopTypeEnum.PICKUP.getCode()) {
            return PICKUP.getCode();
        }
        return SHOP.getCode();
    }

    /**
     * 内置超级管理员角色名称
     */
    public static String systemSuperRoleNameByCode(String adminTypeCode) {
        return fromCode(adminTypeCode).getSystemSuperRoleName();
    }
}
