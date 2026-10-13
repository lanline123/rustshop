// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.shop;

/**
 * 店铺表常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class ShopConstants {
    public static final String SHOP_NOT_NULL = "店铺表名称不能为空";

    public static final String SHOP_OVER_LENGTH = "店铺表名称长度不能超过50";

    public static final String MERCHANT_ID_NOT_NULL = "请选择商户";

    //您不是商户管理员，不能创建店铺
    public static final String SHOP_MERCHANT_USER_NOT_ADMIN = "您不是商户管理员，不能创建店铺";

    //您不是商户管理员，不能修改店铺
    public static final String SHOP_MERCHANT_USER_NOT_ADMIN_UPDATE = "您不是商户管理员，不能修改店铺";

    //您不是商户管理员，不能删除店铺
    public static final String SHOP_MERCHANT_USER_NOT_ADMIN_DELETE = "您不是商户管理员，不能删除店铺";

    //店铺不存在
    public static final String SHOP_NOT_EXIST = "店铺不存在";
}
