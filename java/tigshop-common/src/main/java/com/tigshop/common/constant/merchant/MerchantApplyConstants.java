// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.merchant;

/**
 * 入驻申请常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class MerchantApplyConstants {
    public static final String MERCHANT_APPLY_NOT_NULL = "入驻申请名称不能为空";

    public static final String MERCHANT_APPLY_OVER_LENGTH = "入驻申请名称长度不能超过50";

    public static final String MERCHANT_APPLY_ID_NOT_NULL = "入驻申请id不能为空";

    public static final String MERCHANT_APPLY_STATUS_NOT_NULL = "入驻申请状态不能为空";

    //待审核
    public static final Integer MERCHANT_APPLY_STATUS_WAIT = 1;

    //审核通过
    public static final Integer MERCHANT_APPLY_STATUS_PASS = 10;

    //审核未通过
    public static final Integer MERCHANT_APPLY_STATUS_NOT_PASS = 20;

    //审核成功
    public static final String MERCHANT_APPLY_STATUS_SUCCESS = "审核成功";

    //审核失败
    public static final String MERCHANT_APPLY_STATUS_FAIL = "审核失败";

    //商户申请信息不存在
    public static final String MERCHANT_APPLY_NOT_EXIST = "商户申请信息不存在";

}
