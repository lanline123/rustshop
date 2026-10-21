// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.panel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 保存首页快捷功能配置DTO
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "保存首页快捷功能配置")
public class ShortcutMenuSaveDTO {

    @Schema(description = "功能 key 有序数组（整体覆盖保存）")
    private List<String> list;
}
