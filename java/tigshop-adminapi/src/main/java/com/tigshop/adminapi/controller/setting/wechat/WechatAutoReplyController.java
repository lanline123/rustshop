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

import com.tigshop.bean.dto.setting.wechat.ReplyPayloadDTO;
import com.tigshop.bean.vo.setting.wechat.ReplyPayloadVO;
import com.tigshop.service.setting.wechat.WechatAutoReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公众号自动回复控制器
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@RestController
@RequestMapping("/adminapi/wechat/autoReply")
@Tag(name = "公众号自动回复", description = "公众号关注/默认自动回复配置")
@Validated
@PreAuthorize("@pms.hasPermission('wechatAutoReply')")
public class WechatAutoReplyController {
    @Resource
    private WechatAutoReplyService wechatAutoReplyService;

    @GetMapping("/subscribe")
    @Operation(summary = "获取关注后自动回复")
    public ReplyPayloadVO getSubscribe() {
        return wechatAutoReplyService.getSubscribe();
    }

    @PostMapping("/subscribe/save")
    @Operation(summary = "保存关注后自动回复")
    @PreAuthorize("@pms.hasPermission('wechatAutoReplyManage')")
    public void saveSubscribe(@Valid @RequestBody ReplyPayloadDTO dto) {
        wechatAutoReplyService.saveSubscribe(dto);
    }

    @GetMapping("/default")
    @Operation(summary = "获取默认自动回复")
    public ReplyPayloadVO getDefault() {
        return wechatAutoReplyService.getDefault();
    }

    @PostMapping("/default/save")
    @Operation(summary = "保存默认自动回复")
    @PreAuthorize("@pms.hasPermission('wechatAutoReplyManage')")
    public void saveDefault(@Valid @RequestBody ReplyPayloadDTO dto) {
        wechatAutoReplyService.saveDefault(dto);
    }

    @GetMapping("/loginSuccess")
    @Operation(summary = "获取扫码登录成功回复")
    public ReplyPayloadVO getLoginSuccess() {
        return wechatAutoReplyService.getLoginSuccess();
    }

    @PostMapping("/loginSuccess/save")
    @Operation(summary = "保存扫码登录成功回复")
    @PreAuthorize("@pms.hasPermission('wechatAutoReplyManage')")
    public void saveLoginSuccess(@Valid @RequestBody ReplyPayloadDTO dto) {
        wechatAutoReplyService.saveLoginSuccess(dto);
    }
}
