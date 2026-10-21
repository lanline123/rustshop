// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.im;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.tigshop.common.constant.im.ConfigConstants.CONFIG_ID_NOT_NULL;

/**
  * im配置更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "im配置参数")
public class ConfigUpdateDTO {
    @Schema(description = "im配置ID")
    @NotNull(message = CONFIG_ID_NOT_NULL)
    private Integer configId;

    @Schema(description ="im配置名称")
    private String configName;

    @Schema(description ="im配置图片")
    private String configPic;

    @Schema(description ="im配置排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;
}
