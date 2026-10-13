// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 语言关联
 *
 * @author Tigshop团队
 * @create 2024年12月31日 15:23
 */
@Data
@Schema(description = "语言关联")
public class LocalesRelationVO {
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "地区名称")
    private String name;

    @Schema(description = "浏览器语言标识")
    private String code;

    @Schema(description = "locales_id")
    private Integer localesId;
}
