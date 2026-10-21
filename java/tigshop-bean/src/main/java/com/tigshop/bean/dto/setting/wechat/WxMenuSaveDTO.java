// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.setting.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 微信菜单保存并发布参数DTO
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "微信菜单保存并发布参数")
public class WxMenuSaveDTO {

    @Schema(description = "菜单树（根容器，含一级菜单列表）")
    @NotNull(message = "菜单结构不能为空")
    @Valid
    private WxMenuTree tree;

    /**
     * 菜单树根容器
     */
    @Data
    @Schema(description = "菜单树")
    public static class WxMenuTree {

        @Schema(description = "一级菜单列表，长度 ≤ 3")
        @NotNull(message = "一级菜单列表不能为空")
        @Size(max = 3, message = "一级菜单最多 3 个")
        @Valid
        private List<WxMenuNodeDTO> buttons;
    }
}
