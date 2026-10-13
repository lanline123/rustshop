// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.salesman;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销模式设置创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "分销模式设置参数")
public class SalesmanConfigCreateDTO {
    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "分销模式设置代码")
    private String code;

    @Schema(description = "数据")
    private String data;
}
