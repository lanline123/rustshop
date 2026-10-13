// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat;

/**
 * 公众号自动回复在 {@code config} 表中的 {@code biz_code} 常量集合。
 * <p>
 * M3（自动回复）复用 {@code config} 表存储两条 JSON 记录：
 * <ul>
 *   <li>{@link #SUBSCRIBE} —— 关注后自动回复</li>
 *   <li>{@link #DEFAULT}   —— 关键字未命中时的默认兜底回复</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
public final class WechatAutoReplyBizCodes {

    /** 关注后自动回复 bizCode */
    public static final String SUBSCRIBE = "wechatSubscribeReply";

    /** 默认兜底自动回复 bizCode */
    public static final String DEFAULT = "wechatDefaultReply";

    /** 扫码登录成功后自动回复 bizCode */
    public static final String LOGIN_SUCCESS = "wechatLoginSuccessReply";

    private WechatAutoReplyBizCodes() {
    }
}
