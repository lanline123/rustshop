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

import lombok.AllArgsConstructor;
import lombok.Data;
import me.chanjar.weixin.common.bean.menu.WxMenu;

/**
 * 微信菜单远程调用门面
 * <p>统一封装 WxJava {@code menuCreate}，对调用方屏蔽 {@code WxErrorException} 与底层异常，
 * 把成功 / 失败封装为 {@link PublishResult}，失败文案按需求 §3.3 拼接。
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
public interface WxMenuApiService {

    /**
     * 推送菜单到微信公众号
     *
     * @param wxMenu 已转换为微信官方结构的菜单
     * @return PublishResult
     */
    PublishResult menuCreate(WxMenu wxMenu);

    /**
     * 微信菜单推送结果
     */
    @Data
    @AllArgsConstructor
    class PublishResult {
        /** 是否推送成功 */
        private boolean success;

        /** 失败文案：发布失败（{errcode}）：{errmsg}；成功时为 null */
        private String errMsg;
    }
}
