// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.finance;

import java.util.Map;

/**
 * 发票资质常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class UserInvoiceConstants {

    public static final Integer STATUS_APPROVED = 1;
    public static final Integer STATUS_AUDIT = 2;
    public static final Integer STATUS_REJECTED = 3;

    // 处理状态
    public static final Map<Integer, String> STATUS_NAME = Map.of(
            STATUS_APPROVED, "审核通过",
            STATUS_AUDIT, "待审核",
            STATUS_REJECTED, "审核未通过"
    );

    public static final Integer TITLE_TYPE_PERSONAL = 1;
    public static final Integer TITLE_TYPE_COMPANY = 2;

    // 处理状态
    public static final Map<Integer, String> TITLE_TYPE_NAME = Map.of(
            TITLE_TYPE_PERSONAL, "个人",
            TITLE_TYPE_COMPANY, "公司"
    );

    public static final String USER_INVOICE_ID_NOT_NULL = "发票资质id不能为空";

    public static final String USER_INVOICE_APPLY_REPEAT = "您已申请过增值税专用发票开票资质，请勿重复申请";
}
