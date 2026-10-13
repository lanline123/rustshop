// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.constant.content;

/**
 * 微信直播常量
 *
 * @author Tigshop团队
 * @create 2024年12月04日 09:56
 */
public class WechatLiveConstants {
    public static final String WECHAT_LIVE_TITLE_NOT_NULL = "微信直播名称不能为空";

    public static final String WECHAT_LIVE_TITLE_OVER_LENGTH = "微信直播名称长度不能超过50";

    public static final String WECHAT_LIVE_ID_NOT_NULL = "微信直播id不能为空";

    public static final String WECHAT_LIVE_NOT_EXISTS = "直播不存在";

    //access_token获取失败
    public static final String WECHAT_LIVE_ACCESS_TOKEN_FAIL = "access_token获取失败";

    //获取直播信息失败
    public static final String WECHAT_LIVE_GET_LIVE_INFO_FAIL = "获取直播信息失败";

    //创建直播间失败
    public static final String WECHAT_LIVE_CREATE_FAIL = "创建直播间失败";

    //删除直播间失败
    public static final String WECHAT_LIVE_DELETE_FAIL = "删除直播间失败";

    //获取推流地址失败
    public static final String WECHAT_LIVE_PUSH_URL_FAIL = "获取推流地址失败";

    //直播间不存在
    public static final String WECHAT_LIVE_ROOM_NOT_EXISTS = "直播间不存在";

    //未开通小程序直播权限
    public static final String WECHAT_LIVE_PERMISSION_NOT_ENABLED = "请开通小程序直播权限";
}
