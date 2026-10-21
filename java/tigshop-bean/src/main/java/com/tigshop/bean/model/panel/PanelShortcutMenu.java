// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.model.panel;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 工作台首页快捷功能配置model
 *
 * @author Tigshop团队
 */
@Data
@TableName("panel_shortcut_menu")
@Schema(description = "工作台首页快捷功能配置")
public class PanelShortcutMenu implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "自增ID")
    private Integer id;

    @Schema(description = "商家管理员ID（联合唯一键，取自 token）")
    private Integer adminId;

    @Schema(description = "店铺ID（联合唯一键，取自请求头 X-Shop-Id）")
    private Integer shopId;

    @Schema(description = "[json]有序的功能 key 字符串数组")
    private String menuKeys;

    @Schema(description = "更新时间")
    private Long updateTime;
}
