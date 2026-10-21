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

import com.tigshop.bean.dto.setting.wechat.WxMenuSaveDTO;
import com.tigshop.bean.vo.setting.wechat.SaveAndPublishVO;
import com.tigshop.bean.vo.setting.wechat.WxMenuVO;
import com.tigshop.service.setting.wechat.WechatMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 微信菜单控制器
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@RestController
@RequestMapping("/adminapi/wechat/menu")
@Tag(name = "微信菜单", description = "公众号自定义菜单配置")
@Validated
@PreAuthorize("@pms.hasPermission('wechatMenu')")
public class WechatMenuController {
    @Resource
    private WechatMenuService wechatMenuService;

    @GetMapping("")
    @Operation(summary = "获取微信菜单详情")
    public WxMenuVO get() {
        return wechatMenuService.get();
    }

    @PostMapping("/saveAndPublish")
    @Operation(summary = "保存并发布微信菜单")
    @PreAuthorize("@pms.hasPermission('wechatMenuManage')")
    public SaveAndPublishVO saveAndPublish(@Valid @RequestBody WxMenuSaveDTO saveDTO) {
        return wechatMenuService.saveAndPublish(saveDTO);
    }
}
