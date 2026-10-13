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
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量启停参数
 *
 * @author Tigshop团队
 * @create 2026年04月21日
 */
@Data
@Schema(description = "批量启停参数")
public class SetEnabledDTO {

    @Schema(description = "要启停的主键ID集合")
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "启用状态：0=停用 1=启用")
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;
}
