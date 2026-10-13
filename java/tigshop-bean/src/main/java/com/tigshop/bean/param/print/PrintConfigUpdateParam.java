// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.print;

import com.tigshop.bean.enums.print.PrintPaperSizeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打印配置更新参数
 *
 * @author Tigshop团队
 * @since 2025/7/22
 */
@Data
@Schema(description = "打印配置更新参数")
public class PrintConfigUpdateParam {

    @Schema(description = "配置ID")
    @NotNull(message = "配置ID不能为空")
    private Integer id;

    @Schema(description = "打印模板")
    @NotNull(message = "打印模板不能为空")
    private PrintTemplate template;

    @Schema(description = "纸张尺寸，接受 JSON 数字 58 / 80；不传则保留原值",
            implementation = Integer.class, example = "58")
    private PrintPaperSizeEnum paperSize;
}