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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 地区列表参数
 *
 * @author Tigshop团队
 * @create 2024年11月11日 14:21
 */
@Data
@Schema(description = "地区列表参数")
public class RegionListVO {
    @Schema(description = "主键")
    private Integer regionId;

    @Schema(description = "地区名称")
    @NotNull(message = "地区名称不能为空")
    @Size(max = 200, message = "地区名称最多200个字符")
    private String regionName;

    @Schema(description = "父级id")
    @NotNull(message = "上级地区不能为空")
    private Integer parentId;

    @Schema(description = "级别")
    private Integer level;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "排序值，数值越小越靠前")
    private Integer sortOrder;

    @Schema(description = "首字母")
    private String firstWord;

}
