// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语言api设置
 * @author Tigshop团队
 * @create 2025年01月02日 15:55
 */
@Data
@Schema(description = "语言api设置")
public class ConfigLangDTO {
    @Schema(description = "是否开启")
    private Integer langOn;

    @Schema(description = "语言类型")
    private Integer langType;

    @Schema(description = "语言类型")
    private String langVolcengineAccessKey;

    @Schema(description = "语言类型")
    private String langVolcengineSecret;
}
