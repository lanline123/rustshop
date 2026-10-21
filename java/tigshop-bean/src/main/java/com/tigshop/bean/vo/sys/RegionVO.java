// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.sys;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 地区列表
 *
 * @author Jayce
 * @create 2024年11月11日 14:12
 */
@Data
@Schema(description = "地区列表")
public class RegionVO implements Serializable {
    @Schema(description = "主键")
    private Integer regionId;

    @Schema(description = "地区名称")
    @JsonTranslate
    private String regionName;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "级别")
    private Integer level;

    @Schema(description = "是否热门")
    private Integer isHot;

    @Schema(description = "首字母")
    private String firstWord;

    @Schema(description = "中心点")
    private String centor;
}
