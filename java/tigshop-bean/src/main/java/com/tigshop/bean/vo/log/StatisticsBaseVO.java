// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统计基础表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "统计基础表参数")
public class StatisticsBaseVO {
    @Schema(description = "统计基础表ID")
    private Integer statisticsBaseId;

    @Schema(description ="统计基础表名称")
    private String statisticsBaseName;

    @Schema(description ="统计基础表图片")
    private String statisticsBasePic;

    @Schema(description ="统计基础表排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="统计基础表描述")
    private String statisticsBaseDesc;
}