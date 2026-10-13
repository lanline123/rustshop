// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.service.oauth;

import com.tigshop.bean.vo.common.MiniSceneVO;

/**
 * 微信
 *
 * @author Tigshop团队
 * @create 2025年01月23日 17:08
 */
public interface WechatOAuthService {
    /**
     * 获取小程序二维码
     *
     * @param path      小程序页面路径
     * @param productId 商品ID
     * @return string
     */
    String getMiniCode(String path, Integer productId);

    /**
     * 获取小程序二维码（分销员分享场景，scene 携带 salesmanId 或短码映射）
     *
     * @param path       小程序页面路径
     * @param productId  商品ID
     * @param salesmanId 分销员ID，可空
     * @return base64 小程序码
     */
    String getMiniCode(String path, Integer productId, Integer salesmanId);

    /**
     * 获取小程序二维码（o2o 进店归因场景，scene 携带 shopId）
     *
     * @param path       小程序页面路径
     * @param productId  商品ID
     * @param salesmanId 分销员ID，可空
     * @param shopId     进店归因门店ID，可空或 0 表示自营
     * @return base64 小程序码
     */
    String getMiniCode(String path, Integer productId, Integer salesmanId, Integer shopId);

    /**
     * 解析小程序码 scene 短码
     */
    MiniSceneVO resolveMiniScene(String scene);

    /**
     * 根据完整路径生成小程序二维码（用于装修推广等无固定 id 的场景）
     *
     * @param fullPath 完整小程序页面路径，如 pages/activity/index?previewId=123
     * @return base64 data URL
     */
    String getMiniCodeByFullPath(String fullPath);

    /**
     * 获取公众号access_token
     * @return
     */
    String getMpAccessToken();

    /**
     * 获取小程序access_token
     * @return
     */
    String getMiniAccessToken();

    /**
     * 获取直播小程序access_token
     * @return
     */
    String getLiveMiniAccessToken();

    /**
     * 强制刷新公众号 access_token（删除缓存后重新获取，用于 40001 失效后的重试）
     */
    String refreshMpAccessToken();

    /**
     * 强制刷新小程序 access_token
     */
    String refreshMiniAccessToken();

    /**
     * 强制刷新直播小程序 access_token
     */
    String refreshLiveMiniAccessToken();
}
