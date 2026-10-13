// **---------------------------------------------------------------------+
// ** 文件
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.api.controller.common;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.bean.vo.common.MiniSceneVO;
import com.tigshop.common.core.AjaxResult;
import com.tigshop.service.oauth.WechatOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工具控制器
 *
 * @author Tigshop团队
 * @create 2025年01月23日 14:24
 */
@RestController
@RequestMapping("/api/common/util")
@Tag(name = "部分通用功能的控制器")
public class UtilController {
    @Resource
    private WechatOAuthService wechatOAuthService;

    @GetMapping("/qrCode")
    @Operation(summary = "获取二维码")
    public AjaxResult<String> qrCode(@RequestParam String url) {
        AjaxResult<String> success = AjaxResult.success();
        success.setData(QrCodeUtil.generateAsBase64(url, new QrConfig(), "png"));
        return success;
    }

    @GetMapping("/miniCode")
    @Operation(summary = "获取小程序二维码")
    public String miniCode(@RequestParam String path,
                           @RequestParam Integer id,
                           @RequestParam(required = false) Integer salesmanId,
                           @RequestParam(required = false) Integer shopId) {
        return wechatOAuthService.getMiniCode(path, id, salesmanId, shopId);
    }

    @GetMapping("/miniScene")
    @Operation(summary = "解析小程序码 scene 短码")
    public MiniSceneVO miniScene(@RequestParam String scene) {
        return wechatOAuthService.resolveMiniScene(scene);
    }
}