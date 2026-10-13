// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.settings.areacode;

import com.tigshop.bean.model.setting.AreaCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import static com.tigshop.common.constant.setting.AreaCodeConstants.*;

/**
 * 区号操作参数
 *
 * @author Tigshop团队
 * @create 2024年12月30日 17:19
 */
@Data
@Schema(description = "区号操作参数")
public class AreaCodeSaveParam {

    @Schema(description = "区号")
    @NotBlank(message = AREA_CODE_NOT_BLANK)
    @Size(max = 64, message = AREA_CODE_MAX_SIZE)
    private String code;

    @Schema(description = "名称")
    @NotBlank(message = AREA_CODE_NAME_NOT_BLANK)
    @Size(max = 64, message = AREA_CODE_NAME_MAX_SIZE)
    private String name;

    @Schema(description = "是否开启：1 是 0 否")
    private Integer isAvailable;

    @Schema(description = "是否默认：1 是 0 否")
    private Integer isDefault;

    public AreaCode createAreaCode() {
        return AreaCode.builder()
                .code(this.code)
                .name(this.name)
                .isAvailable(this.isAvailable)
                .isDefault(this.isDefault)
                .build();
    }

}
