// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.decorate;

import cn.hutool.json.JSONObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
  * 页面管理更新参数
  * @author Tigshop团队
  * @create 2024年12月03日 17:17
  */
@Data
@Schema(description = "页面管理参数")
public class DecorateSaveDTO {
    @Schema(description = "页面管理ID")
    private Integer id;

    @Schema(description ="数据")
    private JSONObject data;

    @Schema(description = "父级ID")
    private Integer parentId;

    @Schema(description = "语言ID")
    private Integer localeId;

}
