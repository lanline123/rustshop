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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 微信菜单节点DTO（一级/二级通用；叶子节点 rule 必填，互斥校验由 Service 侧处理）
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "微信菜单节点")
public class WxMenuNodeDTO {

    @Schema(description = "前端生成的临时 uuid，落库保留用于编辑态稳定 key")
    @Size(max = 64, message = "节点ID长度不能超过 64")
    private String id;

    @Schema(description = "菜单名称；一级 ≤ 8 字节，二级 ≤ 16 字节（精确字节校验在 Service 按层级处理）")
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 16, message = "菜单名称长度超限")
    private String name;

    @Schema(description = "同级排序，0 起递增")
    @NotNull(message = "排序值不能为空")
    @Min(value = 0, message = "排序值不能小于 0")
    private Integer sortOrder;

    @Schema(description = "二级菜单列表；存在时忽略 rule 字段")
    @Size(max = 5, message = "二级菜单最多 5 个")
    @Valid
    private List<WxMenuNodeDTO> children;

    @Schema(description = "叶子菜单规则；存在 children 时可为空")
    @Valid
    private WxMenuRuleDTO rule;
}
