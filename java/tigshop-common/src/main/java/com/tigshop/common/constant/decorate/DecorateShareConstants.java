// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.decorate;

/**
 * 售后申请表常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class DecorateShareConstants {
    public static final String _NOT_NULL = "售后申请表名称不能为空";

    public static final String _OVER_LENGTH = "售后申请表名称长度不能超过50";

    public static final String DECORATE_SHARE_ID_NOT_NULL = "售后申请表id不能为空";

    public static final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final int CODE_LENGTH = 6;

    public static final int TOKEN_CODE_LENGTH = 5;

    public static final Long EXPIRE_TIME = 60 * 60 * 24 * 7L;

    public static final String API_URL = "/api/home/share/import?sn=%s&token=%s";

    public static final String URL_EMPTY = "url 不能为空";
}
