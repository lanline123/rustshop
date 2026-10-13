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

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 配置参数
 *
 * @author Jayce
 * @create 2024年11月08日 15:28
 */
@Data
@Schema(description = "配置参数")
public class ConfigDTO {
    @Schema(description = "主键")
    private Short id;

    @Schema(description = "配置编码")
    @NotBlank(message = "配置编码不能为空")
    private String code;

    @Schema(description = "配置数据")
    @NotNull(message = "配置数据不能为空")
    private JSONObject data;

    public ConfigDTO(String code, JSONObject data) {
        this.code = code;
        this.data = data;

    }
}
