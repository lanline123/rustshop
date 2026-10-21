// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.adminapi.controller.setting.wechat;

import com.tigshop.service.setting.wechat.WxKefuCapabilityProbe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 客服消息能力探测控制器
 *
 * <p>暴露 {@code GET /adminapi/wechat/keywordRule/capability}，返回 {@code { kefuEnabled: boolean }}。
 * 前端列表页据此在客服消息不可用时展示降级提示条（规则仍可编辑，但运行期无法主动下发客服消息）。
 *
 * <p>能力判定由 {@link WxKefuCapabilityProbe} 提供（带结果缓存，避免频繁调用微信 API）。
 * 响应由 {@link com.tigshop.common.handler.GlobalResponseAdvice} 统一包装为 {@code AjaxResult}。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@RestController
@RequestMapping("/adminapi/wechat/keywordRule")
@Tag(name = "公众号关键字回复-客服能力探测", description = "客服消息接口可用性探测")
@PreAuthorize("@pms.hasPermission('wechatKeywordReply')")
public class WechatKefuCapabilityController {

    @Resource
    private WxKefuCapabilityProbe wxKefuCapabilityProbe;

    @GetMapping("/capability")
    @Operation(summary = "探测客服消息接口是否可用")
    public Map<String, Object> capability() {
        Map<String, Object> data = new HashMap<>(2);
        data.put("kefuEnabled", wxKefuCapabilityProbe.isEnabled());
        return data;
    }
}
