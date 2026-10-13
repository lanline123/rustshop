// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant;

/**
 * 校验修改字段的常量
 *
 * @author Tigshop团队
 * @create 2024年11月01日 13:33
 */
public class CheckFieldConstants {

    public static final String[] GENERAL_FIELDS = {"is_show", "sort_order", "status"};

    public static final String[] AUTHORITY_FIELDS = {"authority_name", "authority_sn", "route_link", "is_show", "sort_order"};

    public static final String[] BRAND_FIELDS = {"brand_name", "first_word", "brand_is_hot", "is_show", "sort_order"};

    public static final String[] ROLE_FIELDS = {"role_name"};

    public static final String[] USER_FIELDS = {"username", "nickname", "status"};

    public static final String[] CATEGORY_FIELDS = {"category_name", "measure_unit", "is_hot", "is_show", "sort_order"};

    public static final String[] TPL_FIELDS = {"tpl_name"};

    public static final String[] SERVICES_FIELDS = {"product_service_name", "product_service_desc", "default_on", "sort_order"};

    public static final String[] USER_RANK_FIELDS = {"rank_name", "discount", "min_growth_points", "max_growth_points"};

    public static final String[] ARTICLE_CATEGORY_FIELDS = {"article_category_name", "category_sn", "sort_order"};

    public static final String[] ARTICLE_FIELDS = {"article_title", "is_hot", "is_show", "sort_order", "article_sn"};

    public static final String[] EXAMPLE_FIELDS = {"example_name", "is_show", "sort_order"};

    public static final String[] E_CARD_GROUP_FIELDS = {"is_use"};

    public static final String[] RECHARGE_SETTING_FIELDS = {"money", "discount_money", "sort_order", "is_show"};

    public static final String[] SIGN_IN_SETTING_FIELDS = {"sort_order"};

    public static final String[] POINTS_EXCHANGE_FIELDS = {"is_hot", "is_enabled"};

    public static final String[] SUPPLIERS_FIELDS = {"is_show", "is_check", "suppliers_name"};

    public static final String[] PC_NAVIGATION_FIELDS = {"example_name", "is_show", "is_blank", "sort_order"};

    public static final String[] MESSAGE_TYPE_FIELDS = {"name", "is_wechat", "is_mini_program", "is_message", "is_msg", "is_app", "is_ding", "add_time","is_mail","is_wx_work"};

    public static final String[] REGION_FIELDS = {"region_name", "sort_order", "is_hot", "first_word"
    };

    public static final String[] AREA_CODE_FIELDS = {"code", "name", "is_available", "is_default"};

    public static final String[] CURRENCY_FIELDS = {"is_enabled", "is_default", "sort"};

    public static final String[] SALESMAN_CONTENT_FIELDS = {"title", "is_top", "is_available"};

    public static final String[] FRIEND_LINKS_FIELDS = {"link_title", "sort_order"};

    public static final String[] COUPON_FIELDS = {"coupon_name", "coupon_money", "coupon_discount", "min_order_amount", "is_show"};

    public static final String[] PRODUCT_PROMOTION_FIELDS = {"is_available", "sort_order"};

    public static final String[] USER_COMPANY_FIELDS = {};

    public static final String[] ORDER_CONFIG_FIELDS = {};

    public static final String[] BUYER_SHOW_CONFIG_FIELDS = {"sort_order"};

    public static final String[] SHOP_PRODUCT_CATEGORY_FIELDS = {"category_name", "measure_unit", "is_hot", "is_show",
            "sort_order"};
    public static final String[] PRODUCT_FIELDS = {"product_name", "product_status", "is_delete", "product_sn", "product_price", "sort_order", "product_stock", "productSku","is_del"};

    public static final String[] COMMENT_FIELDS = {"is_show", "sort_order", "comment_rank", "is_top", "is_recommend"};

    public static final String[] DECORATE_FIELDS = {"is_show", "sort_order", "decorate_title"};

    public static final String[] SHIPPING_TYPE_FIELDS = {"is_show", "sort_order", "shipping_type_name", "shipping_type_desc", "is_support_cod"};

    public static final String[] VENDOR_FIELDS = {"vendor_name", "status"};

    public static final String[] PRINT_FIELDS = {"status", "is_default"};

    public static final String[] TIP_FIELDS = {"status"};

    /**
     * 授信会员字段更新（用于启用/禁用）
     */
    public static final String[] CREDIT_USER_FIELDS = {"status"};

    public static final String[] ADMIN_USER_POS_FIELDS = {"is_using"};

    public static final String[] ADMIN_USER_KEFU_FIELDS = {"is_using"};

    public static final String[] GALLERY_FIELDS = {"pic_name"};

    public static final String[] SERVICE_STAFF_FIELDS = {"status", "work_status"};

    public static final String[] PRODUCT_STYLE_FIELDS = {"style_name", "status", "sort_order", "pc_image"};
}
