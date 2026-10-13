// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.setting.wechat.impl;

import com.tigshop.service.setting.wechat.WxMenuApiService;
import com.tigshop.service.setting.wechat.WxMpServiceHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.springframework.stereotype.Service;

/**
 * 微信菜单远程调用门面实现
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Slf4j
@Service
public class WxMenuApiServiceImpl implements WxMenuApiService {

    @Resource
    private WxMpServiceHolder wxMpServiceHolder;

    @Override
    public PublishResult menuCreate(WxMenu wxMenu) {
        try {
            WxMpService wxMpService = wxMpServiceHolder.get();
            if (wxMpService == null) {
                return new PublishResult(false, "发布失败：公众号未配置");
            }
            wxMpService.getMenuService().menuCreate(wxMenu);
            return new PublishResult(true, null);
        } catch (WxErrorException e) {
            WxError err = e.getError();
            int errcode = err != null ? err.getErrorCode() : -1;
            String errmsg = err != null && err.getErrorMsg() != null ? err.getErrorMsg() : e.getMessage();
            String msg = String.format("发布失败（%d）：%s", errcode, errmsg);
            log.warn("[WxMenu] menuCreate WxErrorException: {}", msg, e);
            return new PublishResult(false, msg);
        } catch (Exception e) {
            String msg = "发布失败：" + e.getMessage();
            log.error("[WxMenu] menuCreate unexpected error", e);
            return new PublishResult(false, msg);
        }
    }
}
