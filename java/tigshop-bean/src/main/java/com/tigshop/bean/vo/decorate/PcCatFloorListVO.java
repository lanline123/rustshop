// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.decorate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 首页分类栏VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "首页分类栏参数")
public class PcCatFloorListVO {
    @Schema(description = "分类抽屉ID")
    private Integer catFloorId;

    @Schema(description ="最终显示的分类名")
    private String catFloorName;

    @Schema(description ="分类ICO图标")
    private String floorIcoFont;

    @Schema(description ="分类")
    private List<PcCatFloorListCategoryListDTO> catList;
}